# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [0.0.1-SNAPSHOT] - 2026-04-14

### Security fixes

- **#3 — Prompt injection prevention**: Added `PromptValidator` to sanitize user input and block common injection patterns before forwarding to Claude
- **#4 — HTTP Basic authentication**: Secured all API endpoints with Spring Security HTTP Basic auth; credentials loaded from environment variables (`APP_USERNAME`, `APP_PASSWORD`); passwords encoded with BCrypt
- **#5 — Rate limiting (Denial of Wallet protection)**: Integrated Bucket4j (`bucket4j-core 8.10.1`) to cap requests at 10 per minute per IP; returns HTTP 429 when exceeded
- **#6 — POST endpoint**: Replaced `GET /api/anthropic/{message}` with `POST /api/anthropic/chat` accepting a JSON body `{"message": "..."}` to avoid exposing user input in URLs and server logs
- **#7 — Global error handler**: Added `GlobalExceptionHandler` to return generic error responses and prevent stack traces or internal details from leaking to clients
- **#8 — CORS configuration**: Added `CorsConfig` restricting cross-origin requests to allowed origins configured via `CORS_ALLOWED_ORIGINS` environment variable (default: `http://localhost:3000`)
- **#9 — HTTPS/TLS support**: Added SSL/TLS configuration via `application.properties`; HTTPS is opt-in via environment variables (`SSL_ENABLED`, `SSL_KEY_STORE`, `SSL_KEY_STORE_PASSWORD`, `SSL_KEY_ALIAS`, `SERVER_PORT`); see `HTTPS.md` for setup instructions
- **#11 — Logging and audit trail**: Added SLF4J structured logging in `AnthropicController` recording client IP and message/response lengths on each request

### Technical improvements

- **#10 — Spring AI stable**: Upgraded Spring AI from milestone `2.0.0-M4` to stable `1.0.5`
- **JaCoCo coverage**: Added JaCoCo `0.8.13` (required for Java 25) with a minimum 70% line coverage gate enforced at build time
- **Unit tests**: Added test suites for all components — `PromptValidator`, `SecurityConfig`, `RateLimitConfig`, `AnthropicController`, `GlobalExceptionHandler`, `CorsConfig` — using `MockMvc.standaloneSetup()` and `@SpringBootTest` compatible with Spring Boot 4.x

### Added

- `AnthropicController` exposing `POST /api/anthropic/chat` to call Claude via Spring AI
- `ChatRequest` record as the request body DTO
- `PromptValidator` for input sanitization
- `SecurityConfig` for HTTP Basic authentication
- `RateLimitConfig` with per-IP Bucket4j rate limiting
- `GlobalExceptionHandler` for safe error responses
- `CorsConfig` for cross-origin access control
- `HTTPS.md` documenting TLS setup for development and production
- Spring AI Anthropic starter (`spring-ai-starter-model-anthropic`) and BOM (`spring-ai-bom 1.0.5`)
- `spring-boot-starter-security` and `spring-security-crypto`
- `bucket4j-core 8.10.1` for rate limiting
- `springboot4-dotenv` for `.env` file loading
- `ANTHROPIC_API_KEY`, `APP_USERNAME`, `APP_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `SSL_*`, `SERVER_PORT` environment variable mappings in `application.properties`

### Initial

- Spring Boot 4.0.5 project setup with Java 25
- Maven wrapper (`mvnw`)
