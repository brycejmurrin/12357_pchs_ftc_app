import { useEffect, useRef, useCallback } from "react";
import { useSceneStore } from "@/store/sceneStore";
import { useAuthStore } from "@/store/authStore";

// WebSocket Data Plane hook.
// Receives binary Protobuf SceneMessage frames and applies them to scene state.
export function useProtoStream(projectId: string) {
  const wsRef             = useRef<WebSocket | null>(null);
  const setScene          = useSceneStore((s) => s.setScene);
  const applyDelta        = useSceneStore((s) => s.applyDelta);
  const setConnectionStatus = useSceneStore((s) => s.setConnectionStatus);
  const accessToken       = useAuthStore((s) => s.accessToken);

  const connect = useCallback(() => {
    if (!accessToken || !projectId) return;

    setConnectionStatus("connecting");

    const ws = new WebSocket(
      `/ws/projects/${projectId}/scene?token=${encodeURIComponent(accessToken)}`
    );
    ws.binaryType = "arraybuffer";
    wsRef.current = ws;

    ws.onopen = () => setConnectionStatus("connected");

    ws.onmessage = async (event: MessageEvent<ArrayBuffer>) => {
      // Protobuf deserialization will be wired here once generated bindings
      // are available from `bazel build //proto:scene_ts_proto`.
      // For now we log raw byte length for debugging.
      const bytes = new Uint8Array(event.data);
      console.debug("[ProtoStream] received", bytes.byteLength, "bytes");

      // TODO: replace with actual Protobuf parse:
      // const msg = SceneMessage.decode(bytes);
      // if (msg.type === SceneMessage.Type.SCENE_FULL) { ... }
      // if (msg.type === SceneMessage.Type.SCENE_DELTA) { ... }
      void setScene;
      void applyDelta;
    };

    ws.onerror = () => setConnectionStatus("error");

    ws.onclose = () => {
      setConnectionStatus("disconnected");
      // Reconnect after 3s on unexpected close.
      setTimeout(connect, 3000);
    };
  }, [accessToken, projectId, setScene, applyDelta, setConnectionStatus]);

  useEffect(() => {
    connect();
    return () => {
      wsRef.current?.close();
    };
  }, [connect]);
}
