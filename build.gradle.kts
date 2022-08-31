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
        // implementation("org.ktorm:ktorm-support-postgresql:3.5.0")
        implementation("org.ktorm:ktorm-support-mysql:3.5.0")
        // psql driver
        implementation("org.postgresql:postgresql:42.4.0")
        // maria db driver
        implementation("org.mariadb.jdbc:mariadb-java-client:3.0.7")
        // hikari
        implementation("com.zaxxer:HikariCP:5.0.1")
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
        // logging
        implementation("ch.qos.logback:logback-classic:1.2.9")
        // recive conten multiple times
        implementation("io.ktor:ktor-server-double-receive:2.0.2")
        // CORS
        implementation("io.ktor:ktor-server-cors:2.0.2")
        // client call logging
        implementation("io.ktor:ktor-server-call-logging:2.0.2")
        // metrics
        implementation("io.ktor:ktor-server-metrics-micrometer:2.0.2")
        implementation("io.micrometer:micrometer-registry-prometheus:1.8.6")
        // rabbit mq
        // implementation("com.viartemev:the-white-rabbit:0.0.6")
        implementation("com.rabbitmq:amqp-client:5.15.0")
        // redis
        implementation("io.github.crackthecodeabhi:kreds:0.7")
        // ktor testing
        testImplementation("io.ktor:ktor-server-test-host:2.0.2")
        testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.0")
        // tesing
        testImplementation("junit:junit:4.13.2")
        // sqlite for testing
        implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    }
}