import { useUndoStore } from "@/store/undoStore";
import { useSceneStore } from "@/store/sceneStore";

export function Toolbar() {
  const { undo, redo, canUndo, canRedo } = useUndoStore();
  const { activeProjectName } = useSceneStore();

  return (
    <header className="flex items-center gap-2 px-4 h-10 bg-panel border-b border-accent text-sm select-none">
      <span className="font-semibold text-highlight mr-4">Mini Maya</span>

      {activeProjectName && (
        <span className="text-gray-400 mr-4">{activeProjectName}</span>
      )}

      <div className="flex gap-1">
        <button
          onClick={undo}
          disabled={!canUndo}
          className="px-2 py-1 rounded bg-accent disabled:opacity-30 hover:bg-highlight transition-colors"
        >
          ↩ Undo
        </button>
        <button
          onClick={redo}
          disabled={!canRedo}
          className="px-2 py-1 rounded bg-accent disabled:opacity-30 hover:bg-highlight transition-colors"
        >
          ↪ Redo
        </button>
      </div>

      <div className="ml-auto flex gap-2">
        <button className="px-3 py-1 rounded bg-highlight hover:opacity-90 transition-opacity text-xs">
          Render
        </button>
      </div>
    </header>
  );
}
