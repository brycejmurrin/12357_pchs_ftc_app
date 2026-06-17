use wasm_bindgen::prelude::*;
use nalgebra as na;

// Expose a console.log shim for debugging inside WASM.
#[wasm_bindgen]
extern "C" {
    #[wasm_bindgen(js_namespace = console)]
    fn log(s: &str);
}

macro_rules! console_log {
    ($($t:tt)*) => (log(&format_args!($($t)*).to_string()))
}

/// Axis-Aligned Bounding Box for broad-phase collision.
#[wasm_bindgen]
#[derive(Clone, Copy, Debug)]
pub struct Aabb {
    min_x: f32, min_y: f32, min_z: f32,
    max_x: f32, max_y: f32, max_z: f32,
}

#[wasm_bindgen]
impl Aabb {
    #[wasm_bindgen(constructor)]
    pub fn new(min_x: f32, min_y: f32, min_z: f32,
               max_x: f32, max_y: f32, max_z: f32) -> Aabb {
        Aabb { min_x, min_y, min_z, max_x, max_y, max_z }
    }

    pub fn intersects(&self, other: &Aabb) -> bool {
        self.max_x >= other.min_x && self.min_x <= other.max_x &&
        self.max_y >= other.min_y && self.min_y <= other.max_y &&
        self.max_z >= other.min_z && self.min_z <= other.max_z
    }

    pub fn center_x(&self) -> f32 { (self.min_x + self.max_x) * 0.5 }
    pub fn center_y(&self) -> f32 { (self.min_y + self.max_y) * 0.5 }
    pub fn center_z(&self) -> f32 { (self.min_z + self.max_z) * 0.5 }
}

/// Rigid body for simple physics simulation.
#[wasm_bindgen]
pub struct RigidBody {
    pos_x: f32, pos_y: f32, pos_z: f32,
    vel_x: f32, vel_y: f32, vel_z: f32,
    mass:  f32,
    aabb:  Aabb,
}

#[wasm_bindgen]
impl RigidBody {
    #[wasm_bindgen(constructor)]
    pub fn new(x: f32, y: f32, z: f32, half_w: f32, half_h: f32, half_d: f32, mass: f32) -> RigidBody {
        RigidBody {
            pos_x: x, pos_y: y, pos_z: z,
            vel_x: 0.0, vel_y: 0.0, vel_z: 0.0,
            mass,
            aabb: Aabb::new(x - half_w, y - half_h, z - half_d,
                            x + half_w, y + half_h, z + half_d),
        }
    }

    pub fn step(&mut self, dt: f32, gravity_y: f32) {
        // Symplectic Euler integration.
        self.vel_y += gravity_y * dt;
        self.pos_x += self.vel_x * dt;
        self.pos_y += self.vel_y * dt;
        self.pos_z += self.vel_z * dt;

        // Floor at y=0.
        if self.pos_y < 0.0 {
            self.pos_y = 0.0;
            self.vel_y = -self.vel_y * 0.6; // restitution coefficient
        }

        let hw = (self.aabb.max_x - self.aabb.min_x) * 0.5;
        let hh = (self.aabb.max_y - self.aabb.min_y) * 0.5;
        let hd = (self.aabb.max_z - self.aabb.min_z) * 0.5;
        self.aabb = Aabb::new(
            self.pos_x - hw, self.pos_y - hh, self.pos_z - hd,
            self.pos_x + hw, self.pos_y + hh, self.pos_z + hd,
        );
    }

    pub fn apply_impulse(&mut self, ix: f32, iy: f32, iz: f32) {
        if self.mass <= 0.0 { return; }
        let inv_mass = 1.0 / self.mass;
        self.vel_x += ix * inv_mass;
        self.vel_y += iy * inv_mass;
        self.vel_z += iz * inv_mass;
    }

    pub fn pos_x(&self) -> f32 { self.pos_x }
    pub fn pos_y(&self) -> f32 { self.pos_y }
    pub fn pos_z(&self) -> f32 { self.pos_z }
    pub fn vel_x(&self) -> f32 { self.vel_x }
    pub fn vel_y(&self) -> f32 { self.vel_y }
    pub fn vel_z(&self) -> f32 { self.vel_z }
    pub fn aabb(&self)  -> Aabb { self.aabb }
}

/// World: manages a collection of bodies and runs broad-phase AABB checks.
#[wasm_bindgen]
pub struct PhysicsWorld {
    bodies:  Vec<RigidBody>,
    gravity: f32,
}

#[wasm_bindgen]
impl PhysicsWorld {
    #[wasm_bindgen(constructor)]
    pub fn new(gravity: f32) -> PhysicsWorld {
        console_log!("[mini_maya_physics] PhysicsWorld initialized, gravity={}", gravity);
        PhysicsWorld { bodies: Vec::new(), gravity }
    }

    pub fn add_body(&mut self, body: RigidBody) -> usize {
        let idx = self.bodies.len();
        self.bodies.push(body);
        idx
    }

    pub fn step(&mut self, dt: f32) {
        for body in &mut self.bodies {
            body.step(dt, self.gravity);
        }
        self.broad_phase_collisions();
    }

    pub fn body_count(&self) -> usize {
        self.bodies.len()
    }

    pub fn get_pos_x(&self, idx: usize) -> f32 { self.bodies[idx].pos_x }
    pub fn get_pos_y(&self, idx: usize) -> f32 { self.bodies[idx].pos_y }
    pub fn get_pos_z(&self, idx: usize) -> f32 { self.bodies[idx].pos_z }

    /// Write all body positions into a caller-supplied Float32Array (stride 3).
    pub fn write_positions(&self, out: &mut [f32]) {
        for (i, body) in self.bodies.iter().enumerate() {
            let base = i * 3;
            if base + 2 < out.len() {
                out[base]     = body.pos_x;
                out[base + 1] = body.pos_y;
                out[base + 2] = body.pos_z;
            }
        }
    }

    fn broad_phase_collisions(&mut self) {
        // O(n²) AABB sweep — acceptable for small scene counts (<200 bodies).
        // Replace with BVH or spatial hash for larger scenes.
        let len = self.bodies.len();
        for i in 0..len {
            for j in (i + 1)..len {
                let a_aabb = self.bodies[i].aabb;
                let b_aabb = self.bodies[j].aabb;
                if a_aabb.intersects(&b_aabb) {
                    // Simple elastic response: swap velocity components along
                    // the collision normal (axis-aligned approximation).
                    let dx = self.bodies[j].pos_x - self.bodies[i].pos_x;
                    let dy = self.bodies[j].pos_y - self.bodies[i].pos_y;
                    let dz = self.bodies[j].pos_z - self.bodies[i].pos_z;
                    let len_sq = dx*dx + dy*dy + dz*dz;
                    if len_sq > 1e-6 {
                        let scale = 1.0 / len_sq.sqrt();
                        let nx = dx * scale;
                        let ny = dy * scale;
                        let nz = dz * scale;
                        let rel_v_n = (self.bodies[j].vel_x - self.bodies[i].vel_x) * nx
                                    + (self.bodies[j].vel_y - self.bodies[i].vel_y) * ny
                                    + (self.bodies[j].vel_z - self.bodies[i].vel_z) * nz;
                        if rel_v_n < 0.0 {
                            let impulse = rel_v_n * 0.5;
                            self.bodies[i].vel_x += impulse * nx;
                            self.bodies[i].vel_y += impulse * ny;
                            self.bodies[i].vel_z += impulse * nz;
                            self.bodies[j].vel_x -= impulse * nx;
                            self.bodies[j].vel_y -= impulse * ny;
                            self.bodies[j].vel_z -= impulse * nz;
                        }
                    }
                }
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn aabb_intersection() {
        let a = Aabb::new(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        let b = Aabb::new(0.5, 0.5, 0.5, 1.5, 1.5, 1.5);
        let c = Aabb::new(2.0, 2.0, 2.0, 3.0, 3.0, 3.0);
        assert!(a.intersects(&b));
        assert!(!a.intersects(&c));
    }

    #[test]
    fn rigid_body_gravity() {
        let mut body = RigidBody::new(0.0, 5.0, 0.0, 0.5, 0.5, 0.5, 1.0);
        for _ in 0..100 {
            body.step(0.016, -9.81);
        }
        assert!(body.pos_y >= 0.0, "body should not fall below the floor");
    }
}
