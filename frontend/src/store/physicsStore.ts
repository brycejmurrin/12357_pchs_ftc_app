import { create } from "zustand";

interface PhysicsStore {
  ready:        boolean;
  debugEnabled: boolean;
  worker:       Worker | null;

  initWorker:      () => void;
  toggleDebug:     () => void;
  terminateWorker: () => void;
}

export const usePhysicsStore = create<PhysicsStore>()((set, get) => ({
  ready:        false,
  debugEnabled: false,
  worker:       null,

  initWorker: () => {
    const worker = new Worker(
      new URL("../workers/physics.worker.ts", import.meta.url),
      { type: "module" }
    );

    worker.onmessage = (e) => {
      if (e.data.type === "ready") {
        set({ ready: true });
      }
    };

    worker.onerror = (err) => {
      console.error("[Physics Worker]", err);
      set({ ready: false });
    };

    set({ worker });
  },

  toggleDebug: () => set((s) => ({ debugEnabled: !s.debugEnabled })),

  terminateWorker: () => {
    get().worker?.terminate();
    set({ worker: null, ready: false });
  },
}));
