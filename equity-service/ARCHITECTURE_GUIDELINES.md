# Architecture Guidelines — equity-service

This document defines the architectural rules, conventions, and standards for the **equity-service** project. All current and future development must adhere to these guidelines to maintain consistency, security, and maintainability.

---

## 1. Base Stack Details

| Component          | Technology / Version                        |
|--------------------|---------------------------------------------|
| Language           | Java 17                                     |
| Framework          | Spring Boot 3.2.5                           |
| Build Tool         | Apache Maven                                |
| Web Layer          | Spring Web (spring-boot-starter-web)        |
| Persistence        | Spring Data JPA (spring-boot-starter-data-jpa) |
| Security           | Spring Security (spring-boot-starter-security) |
| Token Management   | jjwt 0.12.5 (jjwt-api, jjwt-impl, jjwt-jackson) |
| Database           | H2 In-Memory (runtime scope)               |
| API Documentation  | springdoc-openapi 2.5.0 (Swagger UI)       |
| Monitoring         | Spring Boot Actuator                        |
| Validation         | Spring Boot Starter Validation (Jakarta)    |
| Boilerplate        | Lombok                                      |
| Testing            | Spring Boot Starter Test, Spring Security Test |

---

## 2. Architecture Pattern

The project follows a **Layered (N-Tier) Architecture** with clear separation of concerns.

### Package Structure

```
com.equity.equityservice
├── controller/      # REST controllers — HTTP request/response handling only
├── service/         # Business logic layer
├── repository/      # Data access layer (Spring Data JPA repositories)
├── entity/          # JPA entity classes (database models)
├── dto/             # Data Transfer Objects (request/response payloads)
├── config/          # Configuration classes (Security, OpenAPI, JWT filter)
├── util/            # Utility classes (JWT token operations)
└── exception/       # Custom exceptions and global exception handler
```

### Layer Rules

| Layer        | Responsibility                                  | Allowed Dependencies                  |
|--------------|--------------------------------------------------|---------------------------------------|
| Controller   | Accept HTTP requests, validate input, return responses | Service, DTO                          |
| Service      | Business logic, data transformation              | Repository, Entity, DTO               |
| Repository   | Database operations via Spring Data JPA          | Entity                                |
| Entity       | JPA-annotated database models                    | None (pure data classes)              |
| DTO          | Request/response data carriers                   | None (pure data classes)              |
| Config       | Framework configuration beans                    | Util, Filter                          |
| Util         | Stateless helper/utility methods                 | None (self-contained)                 |
| Exception    | Exception classes and global handler             | DTO (for error responses)             |

### Key Rules

- Controllers must **never** access repositories directly. All data access goes through the service layer.
- Entities must **never** be exposed directly in API responses. Always use DTOs for request and response payloads.
- Service methods handle mapping between entities and DTOs.
- Repository interfaces extend `JpaRepository` and are annotated with `@Repository`.
- Use the `@Builder` pattern (via Lombok) for constructing entities and DTOs.

---

## 3. Security Rules

### Authentication Mechanism

- **JWT (JSON Web Token)** based stateless authentication using the `jjwt` library.
- Token generation and validation are handled by the `JwtUtil` utility class.
- A `JwtAuthenticationFilter` (extending `OncePerRequestFilter`) intercepts every request and validates the Bearer token from the `Authorization` header.
- The filter is registered **before** `UsernamePasswordAuthenticationFilter` in the Spring Security filter chain.

### Session Management

- Sessions are **stateless** (`SessionCreationPolicy.STATELESS`). No server-side session is maintained.
- CSRF protection is **disabled** (appropriate for stateless REST APIs).

### User Management

- Current implementation uses `InMemoryUserDetailsManager` with BCrypt-encoded passwords.
- When migrating to a persistent user store, implement a custom `UserDetailsService` backed by a JPA repository.

### Password Encoding

- All passwords must be encoded using `BCryptPasswordEncoder`. Plain-text passwords are prohibited.

### Public (Unauthenticated) Endpoints

The following URL patterns are accessible without authentication:

| Pattern              | Purpose              |
|----------------------|----------------------|
| `/api/auth/**`       | Authentication APIs  |
| `/swagger-ui/**`     | Swagger UI assets    |
| `/swagger-ui.html`   | Swagger UI entry     |
| `/v3/api-docs/**`    | OpenAPI spec         |
| `/h2-console/**`     | H2 database console  |
| `/actuator/**`       | Actuator endpoints   |

All other endpoints require a valid JWT token in the `Authorization: Bearer <token>` header.

### JWT Configuration

- The JWT signing key is provided via the `JWT_SECRET` environment variable. It must be a Base64-encoded key of at least 256 bits.
- Token expiration is configured via `jwt.expiration` in `application.yml` (default: 3,600,000 ms / 1 hour).
- **Never** hardcode secrets in source code or configuration files. Always use environment variables or a secrets manager.

---

## 4. Exception Handling Policy

### Global Exception Handler

All exceptions are handled centrally by `GlobalExceptionHandler`, annotated with `@RestControllerAdvice`.

### Standard Error Response Format

Every error response follows the `ErrorResponse` DTO structure:

```json
{
  "status": 404,
  "message": "Equity not found with id: 99",
  "timestamp": "2026-03-02T08:00:00",
  "errors": null
}
```

For validation errors, the `errors` field contains a map of field-level messages:

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-03-02T08:00:00",
  "errors": {
    "equityName": "Equity name is required",
    "price": "Price must be positive"
  }
}
```

### Handled Exception Types

| Exception                          | HTTP Status | Description                      |
|------------------------------------|-------------|----------------------------------|
| `ResourceNotFoundException`        | 404         | Entity not found in database     |
| `BadCredentialsException`          | 401         | Invalid username or password     |
| `MethodArgumentNotValidException`  | 400         | Request body validation failure  |
| `Exception` (fallback)             | 500         | Any unhandled exception          |

### Rules for New Exceptions

- Create custom exception classes in the `exception` package for domain-specific errors.
- Register a corresponding `@ExceptionHandler` method in `GlobalExceptionHandler`.
- Always return the `ErrorResponse` DTO. Never expose stack traces or internal details in API responses.
- Log all exceptions at the appropriate level (`error` for server errors, `warn` for client errors).

---

## 5. Logging Standards

### Framework

- Use **SLF4J** via Lombok's `@Slf4j` annotation on every class that requires logging.
- Do **not** instantiate loggers manually. Always use `@Slf4j`.

### Log Levels by Package

| Package / Logger                    | Level  |
|-------------------------------------|--------|
| `com.equity.equityservice`          | DEBUG  |
| `org.springframework.security`      | INFO   |

### Logging Conventions

| Level   | Usage                                                              |
|---------|--------------------------------------------------------------------|
| `ERROR` | Unexpected failures, unhandled exceptions, system errors           |
| `WARN`  | Recoverable issues (e.g., failed login attempts)                   |
| `INFO`  | Significant business events (e.g., entity saved, login successful) |
| `DEBUG` | Detailed flow tracing (e.g., token validation results)             |

### Rules

- Use **parameterized logging** (`log.info("Saving equity: {}", symbol)`) — never string concatenation.
- Log entry and exit of service methods at `INFO` level.
- Log security events (login success/failure) at `INFO` / `WARN` level.
- **Never** log sensitive data such as passwords, tokens, or secrets.

---

## 6. Database Configuration

### Current Setup

| Property                  | Value                     |
|---------------------------|---------------------------|
| Database                  | H2 In-Memory              |
| JDBC URL                  | `jdbc:h2:mem:equitydb`    |
| Driver                    | `org.h2.Driver`           |
| Dialect                   | `H2Dialect`               |
| DDL Auto                  | `update`                  |
| SQL Init Mode             | `always`                  |
| Deferred Initialization   | `true`                    |

### H2 Console

- Enabled at `/h2-console`.
- `web-allow-others` is set to `true` for development access.
- Frame options are disabled in the security configuration to allow the H2 console iframe to render.

### Sample Data

- Initial data is loaded from `src/main/resources/data.sql` on every application startup.
- Use standard SQL `INSERT` statements in `data.sql` for seeding.
- The `defer-datasource-initialization: true` setting ensures Hibernate creates tables before `data.sql` executes.

### Entity Rules

- All entities must be annotated with `@Entity` and `@Table`.
- Use `@Id` with `@GeneratedValue(strategy = GenerationType.IDENTITY)` for primary keys.
- Use `@Column` annotations with explicit `name` and `nullable` attributes.
- Apply unique constraints via `@Column(unique = true)` where appropriate.
- Use Lombok annotations (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) for boilerplate reduction.

### Repository Rules

- All repositories must extend `JpaRepository<Entity, IdType>`.
- Annotate with `@Repository`.
- Use Spring Data derived query methods or `@Query` annotations for custom queries.

---

## 7. Swagger & Actuator Configuration

### Swagger / OpenAPI

| Property                    | Value                                         |
|-----------------------------|-----------------------------------------------|
| Library                     | springdoc-openapi-starter-webmvc-ui 2.5.0     |
| Swagger UI Path             | `/swagger-ui.html`                            |
| API Docs Path               | `/v3/api-docs`                                |
| Operation Sorter            | By HTTP method                                |
| Security Scheme             | Bearer JWT (`bearerAuth`)                     |

#### Annotation Rules

- Every controller must be annotated with `@Tag(name, description)`.
- Every endpoint method must be annotated with `@Operation(summary, description)`.
- Secured controllers must include `@SecurityRequirement(name = "bearerAuth")`.
- The global security scheme is configured in `OpenApiConfig` — do not duplicate it per controller.

### Actuator

| Property                          | Value                          |
|-----------------------------------|--------------------------------|
| Base Path                         | `/actuator`                    |
| Exposed Endpoints                 | `health`, `metrics`, `info`    |
| Health Details                    | `always` shown                 |

#### Rules

- Only expose endpoints that are necessary. Do **not** expose `env`, `beans`, `configprops`, or `shutdown` unless explicitly required and secured.
- When adding new actuator endpoints, update the `management.endpoints.web.exposure.include` list in `application.yml`.

---

## 8. Rules for Future Feature Extensions

### Adding a New Module / Entity

1. **Entity** — Create the JPA entity in the `entity` package with proper annotations and Lombok builders.
2. **DTOs** — Create separate request and response DTOs in the `dto` package with Jakarta Validation annotations.
3. **Repository** — Create a `JpaRepository` interface in the `repository` package.
4. **Service** — Create a service class in the `service` package annotated with `@Service`. Use constructor injection (via `@RequiredArgsConstructor`).
5. **Controller** — Create a REST controller in the `controller` package annotated with `@RestController`, `@RequestMapping`, `@Tag`, and `@SecurityRequirement`.
6. **Exceptions** — Add domain-specific exceptions and register them in `GlobalExceptionHandler`.
7. **Sample Data** — Add `INSERT` statements to `data.sql` for the new entity.

### Adding a New API Endpoint

- Follow RESTful naming conventions: use nouns for resources, HTTP verbs for actions.
- Use `@Valid` on `@RequestBody` parameters to trigger DTO validation.
- Return appropriate HTTP status codes: `200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `404 Not Found`, `500 Internal Server Error`.
- Document the endpoint with `@Operation` and `@Tag` annotations.

### Dependency Management

- Define library versions as properties in `pom.xml` (e.g., `<jjwt.version>0.12.5</jjwt.version>`).
- Use Spring Boot's dependency management (BOM) for Spring-managed dependencies — do not override their versions unless necessary.
- Evaluate new dependencies carefully before adding them. Prefer Spring ecosystem libraries.

### Configuration Management

- All configuration resides in `application.yml`.
- Sensitive values (secrets, credentials) must be externalized via environment variables using the `${ENV_VAR}` syntax.
- Use Spring profiles (`application-{profile}.yml`) when environment-specific configuration is needed (e.g., `dev`, `staging`, `prod`).

### Testing Standards

- Write unit tests for service classes using JUnit 5 and Mockito.
- Write integration tests for controllers using `@SpringBootTest` and `MockMvc`.
- Use `@WebMvcTest` for controller-only slice tests.
- Use `spring-security-test` for authentication/authorization test scenarios.
- Ensure the `JWT_SECRET` environment variable is set when running tests.

### Code Style

- Use Lombok annotations to reduce boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`).
- Use constructor injection over field injection. Annotate with `@RequiredArgsConstructor` and declare dependencies as `private final` fields.
- Follow Java naming conventions: `camelCase` for fields/methods, `PascalCase` for classes, `UPPER_SNAKE_CASE` for constants.
- Organize imports: Java standard library first, then third-party, then project-internal.
