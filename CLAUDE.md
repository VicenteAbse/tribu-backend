# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the application (port 8080)
./mvnw spring-boot:run

# Build
./mvnw clean package

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=BackendApplicationTests

# Full clean build including tests
./mvnw clean install
```

H2 console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:groupmatch`, user: `sa`, no password).

## Architecture

Spring Boot 3.2.5 / Java 17 REST API with layered architecture under `com.groupmatch.app`:

- **Controller** (`group/`, `health/`) — REST endpoints; validates input via Jakarta bean validation
- **Service** (`group/service/`) — business logic; currently uses in-memory `ArrayList`, not yet wired to the database
- **Domain** (`domain/group/`) — JPA entities mapped to H2; `GroupEntity` exists but is not yet used by the service (no `JpaRepository` interface exists yet)
- **Common** (`common/exception/`) — `GlobalExceptionHandler` catches `MethodArgumentNotValidException` and returns structured 400 responses with per-field errors

**Key gap:** `GroupService` stores data in memory, while `GroupEntity` and H2 are set up but disconnected. Integrating them requires adding a `GroupRepository extends JpaRepository<GroupEntity, Long>` and updating the service.

## Conventions

- Validation error messages are written in Spanish (e.g., `"El nombre es obligatorio"`)
- Package structure: `com.groupmatch.app.{feature}.{layer}` (e.g., `group/service/`)
- DTOs are split into `*Request` (input + validation) and `*Response` (output) per feature package; JPA entities live in `domain/{feature}/`
- Constructor-based dependency injection throughout
