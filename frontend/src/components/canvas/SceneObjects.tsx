import { useRef } from "react";
import { useFrame } from "@react-three/fiber";
import { useSceneStore } from "@/store/sceneStore";
import { MeshObject } from "./MeshObject";
import type { Group } from "three";

export function SceneObjects() {
  const nodes = useSceneStore((s) => s.nodes);
  const groupRef = useRef<Group>(null);

  // High-frequency per-frame work happens here, NOT in useState/useEffect.
  useFrame((_state, _delta) => {
    // Physics transform updates from the worker are written directly to
    // node ref matrices here, bypassing React state entirely.
    // (Populated by usePhysicsWorker hook once the WASM worker is active.)
  });

  return (
    <group ref={groupRef}>
      {nodes.map((node) => (
        <MeshObject key={node.id} node={node} />
      ))}
    </group>
  );
}
