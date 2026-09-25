# spring-skeleton

### A skeleton for Spring Boot 4 / Java 25

Included features
1. Stateless JWT authentication with Spring Security's OAuth2 resource server (HS512 tokens issued by `POST /api/auth/login`)
2. Role-based method security (`@PreAuthorize`) driven by a `roles` token claim
3. JPA with PostgreSQL + Flyway migrations
4. RFC 9457 `ProblemDetail` error responses with application error codes
5. Header-based API versioning (`API-Version`, Spring Framework 7)
6. OpenAPI docs via springdoc-openapi (Swagger UI)
7. Structured (ECS) file logging with daily rolling, Tomcat access logs, Actuator health probes
8. Docker Compose dev services (PostgreSQL starts automatically)

Key dependencies
- Spring Boot 4.1.x (Spring Framework 7, Spring Security 7.1, Hibernate 7, Jackson 3)
- Java 25 (LTS), Maven 3.9 via the wrapper
- springdoc-openapi 3.1, Flyway, Lombok

Running locally
1. Install JDK 25 and start Docker.
2. `./mvnw spring-boot:run` — `compose.yaml` starts PostgreSQL and the datasource is wired automatically; Flyway creates the schema.
   - Without Docker: set `SPRING_DOCKER_COMPOSE_ENABLED=false` and point `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` at a running PostgreSQL.
3. Swagger UI: `http://localhost:8090/skeleton/swagger-ui.html`

Configuration (environment variables)
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET_KEY` — at least 64 bytes (HS512); the default is for development only
- `JWT_VALIDITY` — token lifetime, e.g. `3h` or milliseconds (`10800000`)
- `JWT_ISSUER` — `iss` claim written and required on tokens (default `spring-skeleton`)
- `CORS_ALLOWED_ORIGINS`, `CORS_ALLOWED_METHODS`, `CORS_ALLOWED_HEADERS` — comma-separated

Profiles
- `dev` (default): SQL logging, Docker Compose, Flyway baselines an existing schema.
- `prod`: Docker Compose disabled; DB credentials must come from the environment.
- Hibernate runs with `ddl-auto: validate` everywhere — schema changes go in `src/main/resources/db/migration`.

Error format

Errors are `application/problem+json`:
```json
{ "title": "Unauthorized", "status": 401, "detail": "Username or password is incorrect",
  "instance": "/api/auth/login", "errorCode": "ERR_02" }
```
Validation errors add an `errors` object mapping field names to messages.

Build and test
- `./mvnw clean verify` — build, enforcer checks, unit and integration tests (H2 in PostgreSQL mode)
- `./mvnw test -Dtest=AuthenticationIntegrationTest` — single test class
