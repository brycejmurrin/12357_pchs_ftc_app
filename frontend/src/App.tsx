import { useEffect } from "react";
import { SceneCanvas } from "@/components/canvas/SceneCanvas";
import { Toolbar } from "@/components/ui/Toolbar";
import { SceneOutliner } from "@/components/ui/SceneOutliner";
import { PropertiesPanel } from "@/components/ui/PropertiesPanel";
import { StatusBar } from "@/components/ui/StatusBar";
import { useAuthStore } from "@/store/authStore";

export default function App() {
  const { initAuth } = useAuthStore();

  useEffect(() => {
    initAuth();
  }, [initAuth]);

  return (
    <div className="flex flex-col h-screen w-screen bg-canvas">
      <Toolbar />
      <div className="flex flex-1 overflow-hidden">
        <SceneOutliner />
        <main className="flex-1 relative">
          <SceneCanvas />
        </main>
        <PropertiesPanel />
      </div>
      <StatusBar />
    </div>
  );
}
