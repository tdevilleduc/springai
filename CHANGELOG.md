# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Security

- **#25 — Validation manquante sur le body de la requête** : Ajout de `spring-boot-starter-validation`, annotations `@NotBlank` et `@Size(max = 4000)` sur `ChatRequest.message`, `@Valid` sur le `@RequestBody` du contrôleur, et handler `MethodArgumentNotValidException` dans `GlobalExceptionHandler` pour retourner HTTP 400 avec le détail de la contrainte violée — élimine le risque de NPE sur `request.message().length()` et bloque les abus par message surdimensionné avant d'atteindre le modèle

### Added

- **#42 — Spring Boot Actuator**: Ajout de `spring-boot-starter-actuator` et `micrometer-registry-prometheus` pour l'exposition des métriques de performance
  - Endpoints exposés : `health`, `info` (publics), `metrics`, `prometheus` (authentifiés)
  - Timer `anthropic.chat.duration` sur chaque appel au modèle Anthropic
  - Counter `ratelimit.rejected` incrémenté à chaque dépassement de rate limit
  - Gauge `ratelimit.buckets.size` sur la taille du cache de buckets IP
- `AnthropicController` exposing `POST /api/v1/anthropic/chat` to call Claude via Spring AI
- `ChatRequest` record as the request body DTO
- `PromptValidator` for input sanitization
- `SecurityConfig` for HTTP Basic authentication
- `RateLimitConfig` with per-IP Bucket4j rate limiting
- `GlobalExceptionHandler` for safe error responses
- `CorsConfig` for cross-origin access control
- `HTTPS.md` documenting TLS setup for development and production
- Spring AI Anthropic starter (`spring-ai-starter-model-anthropic`) and BOM (`spring-ai-bom 2.0.0-M4`)
- `spring-boot-starter-security`
- `bucket4j-core 8.10.1` for rate limiting
- `springboot4-dotenv` for `.env` file loading
- `ANTHROPIC_API_KEY`, `APP_USERNAME`, `APP_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `SSL_*`, `SERVER_PORT` environment variable mappings in `application.properties`

### Security fixes

- **#39 — Brute force protection on HTTP Basic authentication** : Ajout de `IpUtils` (utilitaire partagé pour l'extraction de l'IP client via `X-Forwarded-For`), `BruteForceProtectionService` (tracks failed attempts per IP with time-based block) and `BruteForceAuthenticationEntryPoint` (increments counter on `BadCredentialsException`, returns HTTP 429 after N consecutive failures, 401 otherwise); wired into `SecurityConfig` via `httpBasic().authenticationEntryPoint()`
  - Configurable via `app.security.max-auth-attempts` (default: 5) and `app.security.block-duration-minutes` (default: 15)
  - Metrics: `security.auth.failure` counter (per IP) and `security.bruteforce.blocked` counter (per IP) exposed via Micrometer
  - Missing credentials (no `Authorization` header) do not increment the counter
- **#38 — CORS GET method unnecessarily allowed**: Removed `GET` from `allowedMethods` in `CorsConfig`; only `POST` and `OPTIONS` are now permitted, matching the actual API surface
- **#28 — Missing HTTP security headers**: Added explicit `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Content-Security-Policy: default-src 'self'` and `Strict-Transport-Security` (HSTS, 1 year, includeSubDomains) in `SecurityConfig`
- **#24 — CORS allowedHeaders overly permissive**: Replaced `allowedHeaders("*")` with an explicit allowlist (`Content-Type`, `Authorization`, `X-Requested-With`) to prevent unauthorized cross-origin requests with credentials; configurable via `CORS_ALLOWED_HEADERS` environment variable
- **#26 — IP Spoofing via X-Forwarded-For**: Added `getClientIp()` in `AnthropicController` to extract the real client IP from the `X-Forwarded-For` header (first entry) before falling back to `getRemoteAddr()`, ensuring rate limiting works correctly behind reverse proxies and load balancers
- **#32 — Stack trace exposure in GlobalExceptionHandler**: `handleGeneric` now logs only the exception message at `ERROR` level; the full stack trace is relegated to `DEBUG` to prevent internal architecture details from appearing in production logs
- **#3 — Prompt injection prevention**: Added `PromptValidator` to sanitize user input and block common injection patterns before forwarding to Claude
- **#4 — HTTP Basic authentication**: Secured all API endpoints with Spring Security HTTP Basic auth; credentials loaded from environment variables (`APP_USERNAME`, `APP_PASSWORD`); passwords encoded with BCrypt
- **#5 — Rate limiting (Denial of Wallet protection)**: Integrated Bucket4j (`bucket4j-core 8.10.1`) to cap requests at 10 per minute per IP; returns HTTP 429 when exceeded
- **#6 — POST endpoint**: Replaced `GET /api/anthropic/{message}` with `POST /api/v1/anthropic/chat` accepting a JSON body `{"message": "..."}` to avoid exposing user input in URLs and server logs
- **#7 — Global error handler**: Added `GlobalExceptionHandler` to return generic error responses and prevent stack traces or internal details from leaking to clients
- **#8 — CORS configuration**: Added `CorsConfig` restricting cross-origin requests to allowed origins configured via `CORS_ALLOWED_ORIGINS` environment variable (default: `http://localhost:3000`)
- **#9 — HTTPS/TLS support**: Added SSL/TLS configuration via `application.properties`; HTTPS is opt-in via environment variables (`SSL_ENABLED`, `SSL_KEY_STORE`, `SSL_KEY_STORE_PASSWORD`, `SSL_KEY_ALIAS`, `SERVER_PORT`); see `HTTPS.md` for setup instructions
- **#11 — Logging and audit trail**: Added SLF4J structured logging in `AnthropicController` recording client IP and message/response lengths on each request

### Improvements

- **#33 — API versioning**: Renamed all endpoints from `/api/anthropic/...` to `/api/v1/anthropic/...` to enable future breaking-change versions without disrupting existing clients

### Technical improvements

- **Logging framework**: Replaced Logback with Log4j2 (`spring-boot-starter-log4j2`); `spring-boot-starter-logging` excluded from all starters
- **#40 — Log4j2 pattern**: Added `log4j2-spring.xml` to replace the Logback-specific `logging.pattern.console` property that was silently ignored
- **Test logging capture**: `GlobalExceptionHandlerTest` now uses a Log4j2 `AbstractAppender`-based `TestListAppender` to assert logging behavior (no throwable at `ERROR`, throwable present at `DEBUG`)
- **JaCoCo coverage**: Added JaCoCo `0.8.13` (required for Java 25) with a minimum 90% line coverage and 80% branch coverage gate enforced at build time
- **Unit tests**: Added test suites for all components — `PromptValidator`, `SecurityConfig`, `RateLimitConfig`, `AnthropicController`, `GlobalExceptionHandler`, `CorsConfig` — using `MockMvc.standaloneSetup()` and `@SpringBootTest` compatible with Spring Boot 4.x

### Initial

- Spring Boot 4.0.5 project setup with Java 25
- Maven wrapper (`mvnw`)
