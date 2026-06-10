FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV BLENDER_VERSION=4.1.1
ENV BLENDER_BIN=/opt/blender/blender

RUN apt-get update && apt-get install -y --no-install-recommends \
    python3.12 python3.12-venv python3-pip \
    wget xz-utils \
    libgl1-mesa-glx libgles2 libegl1 \
    libxi6 libxrender1 libxfixes3 \
    && rm -rf /var/lib/apt/lists/*

RUN wget -q "https://mirror.freedif.org/blender/release/Blender4.1/blender-${BLENDER_VERSION}-linux-x64.tar.xz" \
        -O /tmp/blender.tar.xz \
    && mkdir -p /opt/blender \
    && tar -xf /tmp/blender.tar.xz -C /opt/blender --strip-components=1 \
    && rm /tmp/blender.tar.xz

WORKDIR /app
COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

COPY worker.py .

ENV PYTHONUNBUFFERED=1
ENTRYPOINT ["python3", "worker.py"]
