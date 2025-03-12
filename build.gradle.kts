plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    `java-library`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "dev.jvmdocs.assistant"
version = "0.0.1-SNAPSHOT"
description = "jvm-docs-assistant"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.spring.io/milestone")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(platform(libs.spring.ai.bom))
    implementation(platform(libs.spring.shell.bom))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.actuator)

    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.ai:spring-ai-tika-document-reader")

    implementation(libs.spring.ai.chroma.store.spring.boot.starter)
    implementation(libs.spring.ai.ollama.spring.boot.starter)
    implementation(libs.spring.ai.pdf.document.reader)
    implementation(libs.spring.shell.starter)

    developmentOnly(libs.spring.boot.docker.compose)

    runtimeOnly(libs.spring.boot.devtools)
    runtimeOnly(libs.spring.ai.spring.boot.docker.compose)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.ai:spring-ai-spring-boot-testcontainers")
    testImplementation(libs.spring.shell.starter.test)
    testImplementation("org.testcontainers:chromadb")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.wiremock:wiremock:3.4.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
