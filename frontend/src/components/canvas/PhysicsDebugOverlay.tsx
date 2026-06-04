import { useRef } from "react";
import { useFrame } from "@react-three/fiber";
import { usePhysicsStore } from "@/store/physicsStore";
import type { LineSegments } from "three";

// Renders physics debug wireframes without touching React state each frame.
export function PhysicsDebugOverlay() {
  const lineRef = useRef<LineSegments>(null);
  const debugEnabled = usePhysicsStore((s) => s.debugEnabled);

  useFrame(() => {
    if (!lineRef.current || !debugEnabled) return;
    // Physics worker writes debug geometry directly to this buffer.
    // lineRef.current.geometry.attributes.position.needsUpdate = true;
  });

  if (!debugEnabled) return null;

  return (
    <lineSegments ref={lineRef}>
      <bufferGeometry />
      <lineBasicMaterial color="#00ff88" />
    </lineSegments>
  );
}
