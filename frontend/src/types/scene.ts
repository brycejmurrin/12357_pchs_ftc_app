// Client-side scene graph types.
// Populated by parsing Protobuf payloads from the WebSocket Data Plane.
// Do NOT manually edit generated protobuf bindings in src/generated/.

export interface SceneGeometry {
  vertices: number[];
  normals:  number[];
  uvs:      number[];
  indices:  number[];
  casHash:  string;
}

export interface SceneMaterial {
  id:         string;
  name:       string;
  baseColor:  string;  // hex string
  metallic:   number;
  roughness:  number;
}

export interface SceneNode {
  id:         string;
  name:       string;
  materialId: string | null;
  children:   string[];
  geometry:   SceneGeometry | null;
  material:   SceneMaterial | null;
  transform:  number[];  // 4x4 column-major
}

export interface SceneState {
  id:         string;
  name:       string;
  commitId:   string;
  nodes:      SceneNode[];
  materials:  SceneMaterial[];
}

export type ConnectionStatus = "disconnected" | "connecting" | "connected" | "error";
