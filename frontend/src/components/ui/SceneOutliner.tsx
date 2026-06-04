import { useSceneStore } from "@/store/sceneStore";

export function SceneOutliner() {
  const { nodes, selectedNodeId, selectNode } = useSceneStore();

  return (
    <aside className="w-52 bg-panel border-r border-accent flex flex-col overflow-hidden">
      <div className="px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-widest border-b border-accent">
        Outliner
      </div>
      <ul className="flex-1 overflow-y-auto py-1">
        {nodes.length === 0 && (
          <li className="px-3 py-2 text-xs text-gray-500 italic">
            No objects in scene
          </li>
        )}
        {nodes.map((node) => (
          <li
            key={node.id}
            onClick={() => selectNode(node.id)}
            className={`px-3 py-1.5 text-xs cursor-pointer rounded mx-1 my-0.5 transition-colors ${
              selectedNodeId === node.id
                ? "bg-highlight text-white"
                : "hover:bg-accent text-gray-300"
            }`}
          >
            {node.name}
          </li>
        ))}
      </ul>
    </aside>
  );
}
