import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    application
}

group = "me.vasabi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}



tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
    dependencies {
        // kotlin orm
        implementation("org.ktorm:ktorm-core:3.5.0")
        // webserver
        implementation("io.ktor:ktor-server-core:2.0.2")
        implementation("io.ktor:ktor-server-netty:2.0.2")
        // receive json
        implementation("io.ktor:ktor-server-content-negotiation:2.0.2")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.2")
        // auth - json web token
        implementation("io.ktor:ktor-server-auth:2.0.2")
        implementation("io.ktor:ktor-server-auth-jwt:2.0.2")
        // websocket
        implementation("io.ktor:ktor-server-websockets:2.0.2")
        // hashing utils
        implementation("commons-codec:commons-codec:1.15")
        // psql
        implementation("org.postgresql:postgresql:42.4.0")
        // logging
        implementation("ch.qos.logback:logback-classic:1.2.9")
    }
}