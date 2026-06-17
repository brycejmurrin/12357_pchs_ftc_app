load("@rules_python//python:defs.bzl", "py_binary", "py_library")

package(default_visibility = ["//visibility:public"])

py_library(
    name = "render_worker_lib",
    srcs = ["worker.py"],
)

py_binary(
    name = "render_worker",
    srcs = ["worker.py"],
    main = "worker.py",
    deps = [":render_worker_lib"],
    python_version = "PY3",
)
