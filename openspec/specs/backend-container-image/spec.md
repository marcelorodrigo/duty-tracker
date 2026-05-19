# Backend Container Image

## Purpose
Define the container image characteristics for the Spring Boot backend application, balancing minimal attack surface with operational debuggability.

## Requirements

### Requirement: Backend runs in a distroless JRE container image

The runtime container for the backend application SHALL be built from the
`gcr.io/distroless/java25-debian13` base image (or equivalent distroless Java 25
image) to minimize attack surface by excluding shell, package manager, and
unnecessary OS utilities.

#### Scenario: Container starts and serves HTTP requests

- **WHEN** the container is started with port 8080 exposed
- **THEN** the Spring Boot application SHALL start successfully
- **AND** the Actuator health endpoint at `/actuator/health` SHALL respond with HTTP 200

#### Scenario: Image has no shell or package manager

- **WHEN** the image is inspected
- **THEN** the image SHALL NOT contain `/bin/sh`, `/bin/bash`, or any package manager binary
- **AND** the image SHALL contain only the JRE, application JAR, and minimal Debian 13 runtime libraries

### Requirement: CMD convention matches distroless entrypoint

The Dockerfile SHALL use `CMD ["app.jar"]` (not `ENTRYPOINT ["java", "-jar", "app.jar"]`)
because the distroless base image already defines `ENTRYPOINT ["java", "-jar"]`.

#### Scenario: CMD works with entrypoint

- **WHEN** the container is run without overriding CMD
- **THEN** the JVM SHALL execute `app.jar` as the main application JAR
- **AND** the application SHALL listen on port 8080
