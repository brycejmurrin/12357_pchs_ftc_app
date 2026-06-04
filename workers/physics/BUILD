load("@rules_rust//rust:defs.bzl", "rust_library", "rust_test")
load("@rules_rust//wasm_bindgen:defs.bzl", "rust_wasm_bindgen")

package(default_visibility = ["//visibility:public"])

rust_library(
    name = "minimaya_physics",
    srcs = ["src/lib.rs"],
    edition = "2021",
    crate_type = "cdylib",
    deps = [
        "@crates//:wasm-bindgen",
        "@crates//:js-sys",
        "@crates//:nalgebra",
    ],
)

rust_wasm_bindgen(
    name = "minimaya_physics_wasm",
    wasm_file = ":minimaya_physics",
    bindgen_flags = ["--target", "web"],
)

rust_test(
    name = "physics_tests",
    crate = ":minimaya_physics",
)
