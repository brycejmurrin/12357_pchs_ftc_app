import { useSceneStore } from "@/store/sceneStore";

export function PropertiesPanel() {
  const { nodes, selectedNodeId } = useSceneStore();
  const selected = nodes.find((n) => n.id === selectedNodeId);

  return (
    <aside className="w-60 bg-panel border-l border-accent flex flex-col overflow-hidden">
      <div className="px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-widest border-b border-accent">
        Properties
      </div>
      <div className="flex-1 overflow-y-auto p-3">
        {!selected ? (
          <p className="text-xs text-gray-500 italic">Select an object</p>
        ) : (
          <div className="space-y-3">
            <div>
              <label className="block text-xs text-gray-400 mb-1">Name</label>
              <div className="text-xs font-mono text-white">{selected.name}</div>
            </div>
            <div>
              <label className="block text-xs text-gray-400 mb-1">ID</label>
              <div className="text-xs font-mono text-gray-500 truncate">{selected.id}</div>
            </div>
            {selected.material && (
              <div>
                <label className="block text-xs text-gray-400 mb-1">Material</label>
                <div className="text-xs font-mono text-gray-300">
                  Roughness: {selected.material.roughness?.toFixed(2)}
                </div>
                <div className="text-xs font-mono text-gray-300">
                  Metallic: {selected.material.metallic?.toFixed(2)}
                </div>
              </div>
            )}
            {selected.geometry && (
              <div>
                <label className="block text-xs text-gray-400 mb-1">Geometry</label>
                <div className="text-xs font-mono text-gray-300">
                  Vertices: {selected.geometry.vertices.length / 3}
                </div>
                <div className="text-xs font-mono text-gray-300">
                  Triangles: {selected.geometry.indices.length / 3}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </aside>
  );
}
