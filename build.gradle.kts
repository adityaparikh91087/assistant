
plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

repositories {
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
    api(libs.spring.boot.starter.web)
    api(libs.spring.boot.starter.aop)
    api(libs.spring.boot.starter.actuator)
    api(libs.spring.ai.chroma.store.spring.boot.starter)
    api(libs.spring.ai.openai.spring.boot.starter)
    api(libs.spring.ai.pdf.document.reader)
    api(libs.spring.shell.starter)
    runtimeOnly(libs.spring.boot.docker.compose)
    runtimeOnly(libs.spring.ai.spring.boot.docker.compose)
    runtimeOnly(libs.spring.boot.devtools)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.shell.starter.test)
}

group = "dev.jvmdocs"
version = "0.0.1-SNAPSHOT"
description = "jvm-docs-assistant"
java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
