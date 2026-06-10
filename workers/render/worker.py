"""
Cloud GPU render worker for Mini Maya.

Lifecycle:
  1. Pull a RenderJobMessage (binary Protobuf) from RabbitMQ.
  2. Download scene assets from MinIO.
  3. Execute headless Blender render (CYCLES or EEVEE).
  4. Periodically push low-res preview frames to MinIO and broadcast URLs.
  5. Upload final render to MinIO.
  6. Publish RenderResultMessage and ACK the queue message.

Stateless: ACKs ONLY after successful storage — never before.
Crashes before ACK cause automatic requeue.
"""

import os
import sys
import time
import tempfile
import subprocess
from pathlib import Path

import pika
import structlog
from dotenv import load_dotenv
from minio import Minio

load_dotenv()

log = structlog.get_logger()

# ── Config ────────────────────────────────────────────────────────────────────

RABBITMQ_URL         = os.environ["RABBITMQ_URL"]
MINIO_ENDPOINT       = os.environ["MINIO_ENDPOINT"]
MINIO_ACCESS_KEY     = os.environ["MINIO_ACCESS_KEY"]
MINIO_SECRET_KEY     = os.environ["MINIO_SECRET_KEY"]
MINIO_BUCKET         = os.environ.get("MINIO_BUCKET", "minimaya-assets")
RENDER_JOBS_QUEUE    = os.environ.get("RENDER_JOBS_QUEUE", "render.jobs")
RENDER_RESULTS_QUEUE = os.environ.get("RENDER_RESULTS_QUEUE", "render.results")
PREVIEW_QUEUE        = os.environ.get("PREVIEW_QUEUE", "render.previews")
BLENDER_BIN          = os.environ.get("BLENDER_BIN", "/usr/bin/blender")


# ── MinIO client ──────────────────────────────────────────────────────────────

def make_minio_client() -> Minio:
    secure = not MINIO_ENDPOINT.startswith("localhost")
    return Minio(MINIO_ENDPOINT, access_key=MINIO_ACCESS_KEY,
                 secret_key=MINIO_SECRET_KEY, secure=secure)


# ── Minimal Protobuf helpers ──────────────────────────────────────────────────
# Replace with generated pb2 classes once `bazel build //proto` runs.

def _varint(n: int) -> bytes:
    result = []
    while n > 0x7F:
        result.append((n & 0x7F) | 0x80)
        n >>= 7
    result.append(n)
    return bytes(result)


def _encode_string(field_num: int, value: str) -> bytes:
    enc = value.encode()
    return _varint((field_num << 3) | 2) + _varint(len(enc)) + enc


def decode_render_job_message(data: bytes) -> dict:
    """Hand-rolled Protobuf field reader for RenderJobMessage."""
    fields: dict = {}
    pos = 0
    while pos < len(data):
        tag, shift, b = 0, 0, 0
        while True:
            b = data[pos]; pos += 1
            tag |= (b & 0x7F) << shift
            if not (b & 0x80):
                break
            shift += 7
        field_num = tag >> 3
        wire_type = tag & 0x7
        if wire_type == 0:
            val, shift2 = 0, 0
            while True:
                b = data[pos]; pos += 1
                val |= (b & 0x7F) << shift2
                if not (b & 0x80):
                    break
                shift2 += 7
            fields[field_num] = val
        elif wire_type == 2:
            length_val, shift3 = 0, 0
            while True:
                b = data[pos]; pos += 1
                length_val |= (b & 0x7F) << shift3
                if not (b & 0x80):
                    break
                shift3 += 7
            fields[field_num] = data[pos:pos + length_val]
            pos += length_val
        else:
            break
    return {
        "raw_job":       fields.get(1, b""),
        "minio_endpoint": fields.get(2, b"").decode() if isinstance(fields.get(2), bytes) else "",
        "minio_bucket":   fields.get(3, b"").decode() if isinstance(fields.get(3), bytes) else MINIO_BUCKET,
    }


def encode_render_result(job_id: str, status: str,
                          asset_key: str = "", error: str = "") -> bytes:
    status_val = 4 if status == "COMPLETE" else 5
    payload = _encode_string(1, job_id)
    payload += _varint((2 << 3) | 0) + _varint(status_val)
    if asset_key:
        payload += _encode_string(3, asset_key)
    if error:
        payload += _encode_string(4, error)
    return payload


def encode_preview_frame(job_id: str, sample: int,
                          url: str, progress: float) -> bytes:
    import struct
    frame = _encode_string(1, job_id)
    frame += _varint((2 << 3) | 0) + _varint(sample)
    frame += _encode_string(3, url)
    frame += bytes([(4 << 3) | 5]) + struct.pack("<f", progress)
    return _varint((1 << 3) | 2) + _varint(len(frame)) + frame


# ── Blender helpers ───────────────────────────────────────────────────────────

BLENDER_RENDER_SCRIPT = """
import bpy, sys

args = sys.argv[sys.argv.index("--") + 1:]
scene_file, output_path, engine, samples, width, height, use_denoiser = args
samples = int(samples); width = int(width); height = int(height)
use_denoiser = use_denoiser == "true"

bpy.ops.wm.open_mainfile(filepath=scene_file)
scene = bpy.context.scene
scene.render.engine        = engine
scene.render.resolution_x  = width
scene.render.resolution_y  = height
scene.render.filepath      = output_path
scene.render.image_settings.file_format = "PNG"

if engine == "CYCLES":
    scene.cycles.samples        = samples
    scene.cycles.use_denoising  = use_denoiser
    scene.cycles.device         = "GPU"

bpy.ops.render.render(write_still=True)
"""


def run_blender(scene_path: str, output_path: str, engine: str,
                samples: int, width: int, height: int, use_denoiser: bool) -> None:
    script = Path(tempfile.gettempdir()) / "mm_render.py"
    script.write_text(BLENDER_RENDER_SCRIPT)
    engine_id = "CYCLES" if engine == "CYCLES" else "BLENDER_EEVEE_NEXT"
    cmd = [BLENDER_BIN, "--background", "--python", str(script), "--",
           scene_path, output_path, engine_id, str(samples),
           str(width), str(height), str(use_denoiser).lower()]
    log.info("blender_start", cmd=" ".join(cmd))
    result = subprocess.run(cmd, capture_output=True, text=True, timeout=3600)
    if result.returncode != 0:
        raise RuntimeError(f"Blender failed ({result.returncode}): {result.stderr[-2000:]}")
    log.info("blender_done")


# ── Preview push ──────────────────────────────────────────────────────────────

def push_preview(minio: Minio, channel, job_id: str,
                 sample: int, total: int, render_path: str) -> None:
    if not os.path.exists(render_path):
        return
    try:
        from PIL import Image
        img = Image.open(render_path)
        img.thumbnail((320, 180))
        preview_path = render_path + ".preview.jpg"
        img.save(preview_path, "JPEG", quality=60)
        key = f"previews/{job_id}/sample_{sample:06d}.jpg"
        minio.fput_object(MINIO_BUCKET, key, preview_path, content_type="image/jpeg")
        url = minio.presigned_get_object(MINIO_BUCKET, key)
        progress = min(sample / max(total, 1), 1.0)
        channel.basic_publish(exchange="minimaya.render",
                               routing_key="render.previews",
                               body=encode_preview_frame(job_id, sample, url, progress))
        log.info("preview_pushed", job_id=job_id, sample=sample)
    except Exception as exc:
        log.warning("preview_push_failed", job_id=job_id, error=str(exc))


# ── Job processor ─────────────────────────────────────────────────────────────

def process_job(channel, method, _props, body: bytes, minio: Minio) -> None:
    job_id = "<unknown>"
    try:
        msg    = decode_render_job_message(body)
        job_id = msg["raw_job"][:36].decode(errors="replace")
        log.info("job_received", job_id=job_id, bytes=len(body))

        with tempfile.TemporaryDirectory(prefix="minimaya_") as workdir:
            blend_path  = os.path.join(workdir, "scene.blend")
            output_path = os.path.join(workdir, "render.png")

            minio.fget_object(MINIO_BUCKET, f"scenes/{job_id}/scene.blend", blend_path)
            log.info("scene_downloaded")

            # TODO: parse actual settings from raw_job proto fields.
            run_blender(blend_path, output_path, "CYCLES", 128, 1920, 1080, True)
            push_preview(minio, channel, job_id, 16, 128, output_path)

            result_key = f"renders/{job_id}/final.png"
            minio.fput_object(MINIO_BUCKET, result_key, output_path, content_type="image/png")
            log.info("result_uploaded", key=result_key)

        channel.basic_publish(exchange="minimaya.render",
                               routing_key="render.results",
                               body=encode_render_result(job_id, "COMPLETE", result_key))
        channel.basic_ack(delivery_tag=method.delivery_tag)
        log.info("job_complete", job_id=job_id)

    except Exception as exc:
        log.error("job_failed", job_id=job_id, error=str(exc))
        try:
            channel.basic_publish(exchange="minimaya.render",
                                   routing_key="render.results",
                                   body=encode_render_result(job_id, "FAILED", error=str(exc)))
        except Exception:
            pass
        channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


# ── Main ──────────────────────────────────────────────────────────────────────

def main() -> None:
    log.info("render_worker_starting", blender=BLENDER_BIN, queue=RENDER_JOBS_QUEUE)
    minio = make_minio_client()
    if not minio.bucket_exists(MINIO_BUCKET):
        minio.make_bucket(MINIO_BUCKET)

    connection = pika.BlockingConnection(pika.URLParameters(RABBITMQ_URL))
    channel    = connection.channel()
    channel.exchange_declare("minimaya.render", exchange_type="topic", durable=True)
    channel.queue_declare(RENDER_JOBS_QUEUE, durable=True)
    channel.queue_bind(RENDER_JOBS_QUEUE, "minimaya.render", "render.jobs.#")
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(
        queue=RENDER_JOBS_QUEUE,
        on_message_callback=lambda ch, m, p, b: process_job(ch, m, p, b, minio),
    )
    log.info("render_worker_ready")
    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        channel.stop_consuming()
    finally:
        connection.close()
        log.info("render_worker_stopped")


if __name__ == "__main__":
    main()
