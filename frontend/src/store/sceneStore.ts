import { create } from "zustand";
import { subscribeWithSelector } from "zustand/middleware";
import type { SceneNode, SceneMaterial, ConnectionStatus } from "@/types/scene";

interface SceneStore {
  // Scene data
  sceneId:           string | null;
  activeProjectName: string | null;
  nodes:             SceneNode[];
  materials:         SceneMaterial[];
  selectedNodeId:    string | null;
  connectionStatus:  ConnectionStatus;

  // Actions
  setScene:          (sceneId: string, nodes: SceneNode[], materials: SceneMaterial[]) => void;
  applyDelta:        (upserted: SceneNode[], deletedIds: string[]) => void;
  selectNode:        (id: string | null) => void;
  setConnectionStatus: (status: ConnectionStatus) => void;
  setActiveProject:  (name: string) => void;
}

export const useSceneStore = create<SceneStore>()(
  subscribeWithSelector((set) => ({
    sceneId:           null,
    activeProjectName: null,
    nodes:             [],
    materials:         [],
    selectedNodeId:    null,
    connectionStatus:  "disconnected",

    setScene: (sceneId, nodes, materials) =>
      set({ sceneId, nodes, materials }),

    applyDelta: (upserted, deletedIds) =>
      set((state) => {
        const deletedSet = new Set(deletedIds);
        const existing = state.nodes.filter((n) => !deletedSet.has(n.id));
        const upsertedIds = new Set(upserted.map((n) => n.id));
        const merged = [
          ...existing.filter((n) => !upsertedIds.has(n.id)),
          ...upserted,
        ];
        return { nodes: merged };
      }),

    selectNode: (id) => set({ selectedNodeId: id }),

    setConnectionStatus: (status) => set({ connectionStatus: status }),

    setActiveProject: (name) => set({ activeProjectName: name }),
  }))
);
