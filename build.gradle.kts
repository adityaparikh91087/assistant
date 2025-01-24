
plugins {
    `java-library`
    `maven-publish`
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
    api(libs.org.springframework.boot.spring.boot.starter.web)
    api(libs.org.springframework.boot.spring.boot.starter.aop)
    api(libs.org.springframework.boot.spring.boot.starter.actuator)
    api(libs.org.springframework.ai.spring.ai.chroma.store.spring.boot.starter)
    api(libs.org.springframework.ai.spring.ai.openai.spring.boot.starter)
    api(libs.org.springframework.ai.spring.ai.pdf.document.reader)
    api(libs.org.springframework.shell.spring.shell.starter)
    runtimeOnly(libs.org.springframework.boot.spring.boot.docker.compose)
    runtimeOnly(libs.org.springframework.ai.spring.ai.spring.boot.docker.compose)
    runtimeOnly(libs.org.springframework.boot.spring.boot.devtools)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testImplementation(libs.org.springframework.shell.spring.shell.starter.test)
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
