---
name: backend.instructions
description: "Use when: backend-wide decisions, contribution rules, code style, and PR guidance."
applyTo: "backend/**"
visibility: public
---

**Maintainer:** Team
**Purpose:** Backend-specific conventions, contribution flow, coding standards, and PR guidance for the AI assistant.

## Scope
- Applies to: `backend/**` Java service code, tests, build and runtime configuration.

## Build & Run
- Use Maven wrapper: run `./mvnw clean package` and `./mvnw spring-boot:run` for local runs.
- Keep `pom.xml` changes minimal and documented in the PR description.

## Code Style & Practices
- Java 17+ conventions and project code style should be followed.
- Prefer small, focused classes. Keep methods < 50 lines where practical.
- Use explicit Lombok annotations on DTOs and value classes.

## Configuration
- Keep environment-specific values out of source: use `application.properties` or preferably environment variables.
- Secrets must never be committed.

## Tests
- Unit tests for services and components are required for new features. Keep tests fast and deterministic.
- Integration tests may use the provided profiles; document any DB or external dependency needs in the PR.

## Pull Requests
- Provide a short description of the change, why it's needed and any migration steps.
- Run `./mvnw test` locally and include test results in the PR checks.
- Link related issues and DB migrations if applicable.
