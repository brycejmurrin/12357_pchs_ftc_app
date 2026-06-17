import { Canvas } from "@react-three/fiber";
import { OrbitControls, Grid, GizmoHelper, GizmoViewport } from "@react-three/drei";
import { Suspense } from "react";
import { SceneObjects } from "./SceneObjects";
import { PhysicsDebugOverlay } from "./PhysicsDebugOverlay";

export function SceneCanvas() {
  return (
    <Canvas
      camera={{ position: [5, 5, 5], fov: 60 }}
      gl={{ antialias: true, powerPreference: "high-performance" }}
      shadows
      className="absolute inset-0"
    >
      <color attach="background" args={["#1a1a2e"]} />
      <ambientLight intensity={0.4} />
      <directionalLight
        position={[10, 10, 5]}
        intensity={1.5}
        castShadow
        shadow-mapSize={[2048, 2048]}
      />

      <Suspense fallback={null}>
        <SceneObjects />
      </Suspense>

      <Grid
        args={[30, 30]}
        position={[0, -0.001, 0]}
        cellColor="#0f3460"
        sectionColor="#e94560"
        fadeDistance={40}
      />

      <OrbitControls makeDefault />

      <GizmoHelper alignment="bottom-right" margin={[80, 80]}>
        <GizmoViewport />
      </GizmoHelper>

      <PhysicsDebugOverlay />
    </Canvas>
  );
}
