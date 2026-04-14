# Spring AI — Anthropic Integration

A Spring Boot application demonstrating integration with Anthropic's Claude models via [Spring AI](https://spring.io/projects/spring-ai).

## Prerequisites

- Java 25
- Maven (or use the included `./mvnw` wrapper)
- An [Anthropic API key](https://console.anthropic.com)

## Configuration

Create a `.env` file at the project root:

```env
ANTHROPIC_API_KEY=sk-ant-...
```

> The `.env` file is ignored by git. Never commit your API key.

## Run

```bash
./mvnw spring-boot:run
```

The application starts on port `8080`.

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/anthropic/{message}` | Send a message to Claude and get a response |

### Example

```bash
curl http://localhost:8080/api/anthropic/Hello
```

## Tech Stack

- Spring Boot 4.0.5
- Spring AI 2.0.0-M4
- Spring AI Anthropic starter (`spring-ai-starter-model-anthropic`)
- `springboot4-dotenv` for `.env` file support
