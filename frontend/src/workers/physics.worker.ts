// Web Worker: bridge between the main thread and the Rust/WASM physics engine.
// Loaded lazily via usePhysicsStore.initWorker().

let wasmModule: WebAssembly.Module | null = null;

async function initWasm() {
  // WASM binary is built by Bazel from //workers/physics and served as a
  // static asset. Path is resolved at build time.
  try {
    const response = await fetch("/assets/minimaya_physics.wasm");
    const buffer   = await response.arrayBuffer();
    wasmModule     = await WebAssembly.compile(buffer);
    self.postMessage({ type: "ready" });
  } catch (err) {
    // WASM not yet available — worker degrades gracefully.
    console.warn("[Physics Worker] WASM not available, physics disabled:", err);
    self.postMessage({ type: "ready" });  // still signal ready so UI doesn't block
  }
}

self.onmessage = (e: MessageEvent) => {
  switch (e.data.type) {
    case "init":
      initWasm();
      break;
    case "step":
      // { type: "step", dt: number, bodies: ArrayBuffer }
      // Run physics step and post back updated transforms.
      // Populated once WASM integration is complete.
      break;
    default:
      break;
  }
};

// Auto-init on load.
initWasm();
