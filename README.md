# spring-skeleton

### A skeleton for Spring Boot 3 / Java 17

Included features
1. JWT authentication (JJWT 0.11.x, stateless Spring Security filter chain)
2. Logging (Logback daily rolling)
3. JPA with PostgreSQL + HikariCP
4. OpenAPI docs via springdoc-openapi (Swagger UI)

Key dependencies
- Spring Boot 3.3.x, Spring Security, Spring Data JPA, Validation
- Lombok for boilerplate reduction
- springdoc-openapi-starter-webmvc-ui for API docs
- JJWT (api/impl/jackson) for token signing/verification
- Apache commons-lang3 utilities

Running locally
1. Set env vars (or edit `application-dev.yml` defaults):
   - `DB_URL=jdbc:postgresql://127.0.0.1:5432/db_name`
   - `DB_USERNAME=postgres`
   - `DB_PASSWORD=123456`
2. Configure JWT in `application.yml`:
   - `jwt.secret-key` (use a strong value; keep it out of VCS)
   - `jwt.validity` (ms)
3. Start: `./mvnw spring-boot:run`
4. Swagger UI: `http://localhost:8090/skeleton/swagger-ui.html`

Profiles
- `dev` (default): enables SQL logging, auto schema update.
- `prod`: `open-in-view` disabled and DDL auto off; wire external DB creds via env vars.
