Generated using github copilot:

# JVM Docs Assistant

JVM Docs Assistant is a Spring Boot application that uses Spring AI and OpenAI with Chroma vector store to provide intelligent query responses based on a predefined knowledge base. The application leverages vector-based search capabilities to find similar documents and construct responses using a system prompt template.

## Project Structure

```
.DS_Store
.gitignore
.gradle/
.idea/
.mvn/
.vscode/
bin/
build/
src/
target/
build.gradle.kts
compose.yaml
gradle.properties
gradlew
gradlew.bat
jvmdocs.log
mvnw
mvnw.cmd
pom.xml
README.md
settings.gradle.kts
```

## Prerequisites

- Java 21
- Maven 3.9.9
- Gradle 8.12.1

## Getting Started

### Building the Project

To build the project, run the following command:

```sh
./gradlew build
```

### Running the Application

To run the application, use the following command:

```sh
./gradlew bootRun
```

### Running Tests

To run the tests, use the following command:

```sh
./gradlew test
```

## Configuration

The application configuration is defined in the following files:

- application.yaml
- 

gradle.properties


- 

settings.gradle.kts



## Key Components

### Main Application

The main application class is 

Application

.

### Shell Commands

The shell commands are defined in the 

SpringAssistantCommand

 class.

### Documentation Service

The documentation service is implemented in the 

DocumentationService

 class.

### Ingestion Service

The ingestion service is implemented in the 

IngestionService

 class.

### Chat Controller

The chat controller is implemented in the 

ChatController

 class.

### Hints Registrar

The hints registrar is implemented in the 

HintsRegistrar

 class.

### Shell Configuration

The shell configuration is defined in the 

ShellConfig

 class.

## License

This project is licensed under the Apache License, Version 2.0. See the LICENSE file for details.