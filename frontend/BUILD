load("@aspect_rules_js//js:defs.bzl", "js_library")
load("@aspect_rules_ts//ts:defs.bzl", "ts_project")
load("@aspect_rules_js//npm:defs.bzl", "npm_package")

package(default_visibility = ["//visibility:public"])

ts_project(
    name = "mini_maya_ts",
    srcs = glob(
        ["src/**/*.ts", "src/**/*.tsx"],
        exclude = ["src/**/*.test.*"],
    ),
    tsconfig = "tsconfig.json",
    declaration = True,
    deps = [
        "//:node_modules/react",
        "//:node_modules/react-dom",
        "//:node_modules/@react-three/fiber",
        "//:node_modules/@react-three/drei",
        "//:node_modules/three",
        "//:node_modules/zustand",
        "//:node_modules/protobufjs",
    ],
)
