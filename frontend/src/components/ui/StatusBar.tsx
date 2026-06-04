import { useSceneStore } from "@/store/sceneStore";
import { usePhysicsStore } from "@/store/physicsStore";

export function StatusBar() {
  const connectionStatus = useSceneStore((s) => s.connectionStatus);
  const physicsReady = usePhysicsStore((s) => s.ready);

  const statusColor =
    connectionStatus === "connected"
      ? "text-green-400"
      : connectionStatus === "connecting"
      ? "text-yellow-400"
      : "text-red-400";

  return (
    <footer className="flex items-center gap-4 px-4 h-6 bg-panel border-t border-accent text-xs select-none text-gray-400">
      <span className={statusColor}>● {connectionStatus}</span>
      <span>Physics: {physicsReady ? "ready" : "loading…"}</span>
      <span className="ml-auto">Mini Maya v0.1.0</span>
    </footer>
  );
}
