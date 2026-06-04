load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "mini_maya_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
        artifact("org.springframework.boot:spring-boot-starter-web"),
        artifact("org.springframework.boot:spring-boot-starter-websocket"),
        artifact("org.springframework.boot:spring-boot-starter-data-jpa"),
        artifact("org.springframework.boot:spring-boot-starter-security"),
        artifact("org.springframework.boot:spring-boot-starter-amqp"),
        artifact("org.springframework.boot:spring-boot-starter-validation"),
        artifact("io.jsonwebtoken:jjwt-api"),
        artifact("io.jsonwebtoken:jjwt-impl"),
        artifact("io.jsonwebtoken:jjwt-jackson"),
        artifact("org.postgresql:postgresql"),
        artifact("com.google.protobuf:protobuf-java"),
        artifact("io.minio:minio"),
        artifact("org.flywaydb:flyway-core"),
        artifact("org.flywaydb:flyway-database-postgresql"),
        "//proto:scene_java_proto",
        "//proto:render_java_proto",
        "//proto:vcs_java_proto",
    ],
)

java_binary(
    name = "mini_maya_server",
    main_class = "com.minimaya.MiniMayaApplication",
    runtime_deps = [":mini_maya_lib"],
)
