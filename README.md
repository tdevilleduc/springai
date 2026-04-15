# Spring AI — Anthropic Integration

A Spring Boot application demonstrating secure integration with Anthropic's Claude models via [Spring AI](https://spring.io/projects/spring-ai).

## Prerequisites

- Java 25
- Maven (or use the included `./mvnw` wrapper)
- An [Anthropic API key](https://console.anthropic.com)

## Configuration

Create a `.env` file at the project root:

```env
ANTHROPIC_API_KEY=sk-ant-...
APP_USERNAME=admin
APP_PASSWORD=your-secure-password
```

> The `.env` file is ignored by git. Never commit your API key or credentials.

### All environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ANTHROPIC_API_KEY` | *(required)* | Anthropic API key |
| `APP_USERNAME` | `admin` | HTTP Basic auth username |
| `APP_PASSWORD` | *(required)* | HTTP Basic auth password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated list of allowed origins |
| `SSL_ENABLED` | `false` | Enable HTTPS/TLS |
| `SSL_KEY_STORE` | `classpath:keystore.p12` | Path to the PKCS12 keystore |
| `SSL_KEY_STORE_PASSWORD` | `changeit` | Keystore password |
| `SSL_KEY_ALIAS` | `springai` | Key alias in the keystore |
| `SERVER_PORT` | `8443` | Server port |

## Run

```bash
./mvnw spring-boot:run
```

## API

All endpoints require HTTP Basic authentication.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/anthropic/chat` | Send a message to Claude and get a response |

### Example

```bash
curl -u admin:your-secure-password \
     -X POST http://localhost:8443/api/v1/anthropic/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "Hello, Claude!"}'
```

### Rate limiting

Requests are limited to **10 per minute per IP address**. Exceeding this limit returns HTTP 429.

### Error handling

All errors return a generic message without exposing internal details or stack traces.

## HTTPS

See [HTTPS.md](HTTPS.md) for instructions on generating a self-signed certificate for development and configuring a CA-signed certificate for production.

## Build & Test

```bash
./mvnw verify
```

Tests require a minimum of **70% line coverage** (enforced by JaCoCo). The build fails if coverage drops below this threshold.

## Tech Stack

| Component | Version |
|-----------|---------|
| Spring Boot | 4.0.5 |
| Spring AI | 1.0.5 |
| Spring Security | (included with Boot) |
| Bucket4j | 8.10.1 |
| JaCoCo | 0.8.13 |
| Java | 25 |
| `springboot4-dotenv` | — |

## Security features

- **Authentication**: HTTP Basic auth on all endpoints
- **Input validation**: Prompt injection detection and sanitization
- **Rate limiting**: Per-IP cap to prevent Denial of Wallet attacks
- **CORS**: Configurable allowed origins
- **HTTPS**: Optional TLS via environment variables
- **Error handling**: Generic responses to avoid information leakage
- **Audit logging**: Client IP and message/response lengths logged on each request
