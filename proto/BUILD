load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_jvm_external//:defs.bzl", "java_proto_library")

package(default_visibility = ["//visibility:public"])

# ── scene.proto ──────────────────────────────────────────────────────────────
proto_library(
    name = "scene_proto",
    srcs = ["scene.proto"],
)

java_proto_library(
    name = "scene_java_proto",
    deps = [":scene_proto"],
)

# ── vcs.proto ────────────────────────────────────────────────────────────────
proto_library(
    name = "vcs_proto",
    srcs = ["vcs.proto"],
)

java_proto_library(
    name = "vcs_java_proto",
    deps = [":vcs_proto"],
)

# ── render.proto ─────────────────────────────────────────────────────────────
proto_library(
    name = "render_proto",
    srcs = ["render.proto"],
)

java_proto_library(
    name = "render_java_proto",
    deps = [":render_proto"],
)
