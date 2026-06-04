workspace(name = "mini_maya")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# ─────────────────────────────────────────────────────────────
# Protocol Buffers
# ─────────────────────────────────────────────────────────────
http_archive(
    name = "rules_proto",
    sha256 = "6fb6767d1bef535310547e03247f7518b03487740c11b6c6adb7952033fe1295",
    strip_prefix = "rules_proto-6.0.2",
    urls = ["https://github.com/bazelbuild/rules_proto/archive/refs/tags/6.0.2.tar.gz"],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies")
rules_proto_dependencies()

# ─────────────────────────────────────────────────────────────
# Java / Spring Boot (rules_jvm_external for Maven deps)
# ─────────────────────────────────────────────────────────────
http_archive(
    name = "rules_jvm_external",
    sha256 = "d31e369b854322ca5098ea12c69d7175ded971435e55c18dd9dd5f29cc5249ac",
    strip_prefix = "rules_jvm_external-6.1",
    urls = ["https://github.com/bazelbuild/rules_jvm_external/archive/6.1.tar.gz"],
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")
rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")
rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")
maven_install(
    artifacts = [
        "org.springframework.boot:spring-boot-starter-web:3.3.0",
        "org.springframework.boot:spring-boot-starter-websocket:3.3.0",
        "org.springframework.boot:spring-boot-starter-data-jpa:3.3.0",
        "org.springframework.boot:spring-boot-starter-security:3.3.0",
        "org.springframework.boot:spring-boot-starter-amqp:3.3.0",
        "org.springframework.boot:spring-boot-starter-validation:3.3.0",
        "io.jsonwebtoken:jjwt-api:0.12.5",
        "io.jsonwebtoken:jjwt-impl:0.12.5",
        "io.jsonwebtoken:jjwt-jackson:0.12.5",
        "org.postgresql:postgresql:42.7.3",
        "com.google.protobuf:protobuf-java:3.25.3",
        "com.google.protobuf:protobuf-java-util:3.25.3",
        "io.minio:minio:8.5.9",
        "org.flywaydb:flyway-core:10.12.0",
        "org.flywaydb:flyway-database-postgresql:10.12.0",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
        "https://repo.spring.io/milestone",
    ],
)

# ─────────────────────────────────────────────────────────────
# TypeScript / Node (aspect_rules_js)
# ─────────────────────────────────────────────────────────────
http_archive(
    name = "aspect_rules_js",
    sha256 = "75c25a0f15a9e4592bbda45b57aa089e4bf17f9176fd735351e8c6444df87b52",
    strip_prefix = "rules_js-2.1.0",
    urls = ["https://github.com/aspect-build/rules_js/archive/refs/tags/v2.1.0.tar.gz"],
)

load("@aspect_rules_js//js:repositories.bzl", "rules_js_dependencies")
rules_js_dependencies()

http_archive(
    name = "aspect_rules_ts",
    sha256 = "909668a5f5c2f50c4d6a2ad77e33e1e12862e93b80e8b9ad8fe5af3305f8c780",
    strip_prefix = "rules_ts-3.3.1",
    urls = ["https://github.com/aspect-build/rules_ts/archive/refs/tags/v3.3.1.tar.gz"],
)

load("@aspect_rules_ts//ts:repositories.bzl", "rules_ts_dependencies")
rules_ts_dependencies(ts_version_from = "//frontend:package.json")

# ─────────────────────────────────────────────────────────────
# Rust / WebAssembly
# ─────────────────────────────────────────────────────────────
http_archive(
    name = "rules_rust",
    sha256 = "6357de5982dd32526e02278221bb8d0c9e0e93f37f7edc8d2063f006cc47af1e",
    urls = ["https://github.com/bazelbuild/rules_rust/releases/download/0.49.3/rules_rust-v0.49.3.tar.gz"],
)

load("@rules_rust//rust:repositories.bzl", "rules_rust_dependencies", "rust_register_toolchains")
rules_rust_dependencies()
rust_register_toolchains(edition = "2021", versions = ["1.78.0"])

load("@rules_rust//wasm_bindgen:repositories.bzl", "rust_wasm_bindgen_dependencies", "rust_wasm_bindgen_register_toolchains")
rust_wasm_bindgen_dependencies()
rust_wasm_bindgen_register_toolchains()

# ─────────────────────────────────────────────────────────────
# Python
# ─────────────────────────────────────────────────────────────
http_archive(
    name = "rules_python",
    sha256 = "9d04041ac92a0985e344235f5d946f71ac543f1b1565f2cdbc9a2aaee8adf55b",
    strip_prefix = "rules_python-0.33.2",
    urls = ["https://github.com/bazelbuild/rules_python/releases/download/0.33.2/rules_python-0.33.2.tar.gz"],
)

load("@rules_python//python:repositories.bzl", "py_repositories", "python_register_toolchains")
py_repositories()
python_register_toolchains(
    name = "python3_12",
    python_version = "3.12",
)
