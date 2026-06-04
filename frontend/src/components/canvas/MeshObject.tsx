import { useRef, useCallback } from "react";
import { ThreeEvent } from "@react-three/fiber";
import type { Mesh } from "three";
import { useSceneStore } from "@/store/sceneStore";
import type { SceneNode } from "@/types/scene";

interface Props {
  node: SceneNode;
}

export function MeshObject({ node }: Props) {
  const meshRef = useRef<Mesh>(null);
  const selectNode = useSceneStore((s) => s.selectNode);
  const selectedId = useSceneStore((s) => s.selectedNodeId);
  const isSelected = selectedId === node.id;

  const handleClick = useCallback(
    (e: ThreeEvent<MouseEvent>) => {
      e.stopPropagation();
      selectNode(node.id);
    },
    [node.id, selectNode]
  );

  if (!node.geometry) return null;

  return (
    <mesh
      ref={meshRef}
      onClick={handleClick}
      castShadow
      receiveShadow
    >
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          args={[new Float32Array(node.geometry.vertices), 3]}
        />
        {node.geometry.normals.length > 0 && (
          <bufferAttribute
            attach="attributes-normal"
            args={[new Float32Array(node.geometry.normals), 3]}
          />
        )}
        <bufferAttribute
          attach="index"
          args={[new Uint32Array(node.geometry.indices), 1]}
        />
      </bufferGeometry>
      <meshStandardMaterial
        color={isSelected ? "#e94560" : node.material?.baseColor ?? "#ffffff"}
        metalness={node.material?.metallic ?? 0}
        roughness={node.material?.roughness ?? 0.5}
        wireframe={false}
      />
    </mesh>
  );
}
