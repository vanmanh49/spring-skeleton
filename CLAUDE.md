# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Spring Boot **4.1.x** on **Java 25** (Spring Framework 7, Spring Security 7.1, Hibernate 7, Jackson 3, JUnit 6, JSpecify), Maven 3.9 wrapper, Lombok, springdoc-openapi 3.1, Flyway, PostgreSQL (runtime) / H2 in PostgreSQL mode (tests).

## Commands

```bash
./mvnw spring-boot:run                      # run (profile "dev"; Docker Compose starts PostgreSQL from compose.yaml)
./mvnw clean verify                         # build + enforcer (Java 25+, Maven 3.9+) + all tests
./mvnw test -Dtest=JwtTokenServiceTest      # single test class
./mvnw test -Dtest=AuthenticationIntegrationTest#login_withWrongPassword_returns401Problem   # single method
./mvnw clean package -Dmaven.test.skip=true # skip tests (-DskipTests no longer skips AOT processing in Boot 4.1)
```

No linter/formatter plugin is configured.

- App: `http://localhost:8090/skeleton` (context path `/skeleton`), Swagger UI at `/skeleton/swagger-ui.html`.
- Without Docker: `SPRING_DOCKER_COMPOSE_ENABLED=false` plus `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`.
- Other env vars: `JWT_SECRET_KEY` (≥ 64 bytes), `JWT_VALIDITY` (`3h` or ms), `JWT_ISSUER`, `CORS_ALLOWED_ORIGINS|METHODS|HEADERS`.
- Profiles: `dev` (default; SQL logging, Flyway `baseline-on-migrate`), `prod` (Compose disabled), `test` (H2, context path `/`).

## Architecture

Layered: `controller` → `service` (interfaces; `JwtTokenService` is concrete) / `service.impl` → `repository` (Spring Data JPA) → `entity`. Supporting packages: `config` (security, JWT encoder/decoder, typed properties, OpenAPI), `handler` (ProblemDetail error handling), `common` (error codes, security constants), `dto` (records). Every package has a `package-info.java` marked `@NullMarked` (JSpecify) — add one to new packages.

**Authentication flow**
- `POST /api/auth/login` → `AuthenticationServiceImpl` authenticates through the `AuthenticationManager` (built by Spring Security from the `UserDetailServiceImpl` + BCrypt `PasswordEncoder` beans), then `JwtTokenService` issues an HS512 JWT (`sub`, `iss`, `iat`, `exp`, `roles`).
- Roles are taken from the `UserDetails` principal, not `Authentication.getAuthorities()` — Spring Security 7 adds factor authorities like `FACTOR_PASSWORD` there.
- Protected requests use Spring Security's OAuth2 resource server (`JwtConfig`: `NimbusJwtDecoder` with issuer validation). Authorities come from the `roles` claim (no DB lookup per request), mapped without a prefix. `GrantedAuthorityDefaults("")` makes `hasRole('ADMINISTRATOR')` match that role code.
- `JwtProperties` / `CorsProperties` are `@ConfigurationProperties` records (`jwt.*`, `cors.*`); `JwtProperties` validates the key length at startup.
- Public paths live in `SecurityConstants.ALLOWED_URLS` / `SWAGGER_RESOURCES`; everything else requires auth. CORS allows no credentials (bearer tokens only).

**Errors (RFC 9457)**
- `ApplicationErrorHandler` extends `ResponseEntityExceptionHandler`. Every error body is a `ProblemDetail` with an `errorCode` property (`ErrorCode.PROPERTY`); validation errors add an `errors` field map.
- Security's 401/403 responses from the filter chain are routed to the same handler through the `handlerExceptionResolver` bean (see `WebSecurityConfig`), so they are ProblemDetails too.
- `ErrorCode` holds the code, HTTP status and message key (`error.ERR_xx` in `src/main/resources/messages.properties`, resolved via Spring's `MessageSource`). To add an error, add an enum constant and a message.
- For domain errors throw `BusinessException(ErrorCode, args...)` (an `ErrorResponseException`).
- Successful responses are wrapped in `ApiResponse<T>` (`{data, success}`).

**API versioning** — configured with `spring.mvc.apiversion.*` in `application.yml` (header `API-Version`, default and supported `1`). Controllers declare `@RequestMapping(path = "/api/...", version = "1+")`. Add new versions to `spring.mvc.apiversion.supported`.

**Persistence** — the schema is owned by Flyway (`src/main/resources/db/migration`, SQL must run on PostgreSQL and H2). Hibernate runs `ddl-auto: validate`, so entity changes need a new migration. `@EnableJpaAuditing` fills `User.createdAt`/`updatedAt`; `User` has `@Version`.

**Other cross-cutting details**
- `@EnableResilientMethods(proxyTargetClass = true)` enables `@Retryable` / `@ConcurrencyLimit`. Class proxies are required because the annotation is looked up on the invoked method, which for an interface proxy is the interface method. `UserDetailServiceImpl` retries only `TransientDataAccessException`.
- Virtual threads are enabled. Code uses Jackson 3 (`tools.jackson.*`); annotations stay in `com.fasterxml.jackson.annotation`.
- Logging is configured by properties only (no `logback-spring.xml`): ECS JSON to `logs/<profile>/spring_skeleton.log`, rolled into `logs/archived/<profile>/`; Tomcat access logs go to `logs/<profile>/`.
- Surefire attaches Mockito as a `-javaagent` (JDK 21+ restricts dynamic agent loading).

## Tests

- Unit tests: Mockito (`service/impl/*Test`), a real encoder/decoder round trip (`JwtTokenServiceTest`), and a small Spring context that checks retry behavior (`UserDetailServiceImplRetryTest`).
- Integration tests extend `integration/AbstractIntegrationTest` (`@SpringBootTest` + MockMvc + `test` profile + `@Transactional`). It seeds `adminuser`/`editoruser` and provides `obtainToken(...)`. `RestTestClientIntegrationTest` adds `@AutoConfigureRestTestClient`.
