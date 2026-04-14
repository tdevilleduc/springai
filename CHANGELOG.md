# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [0.0.1-SNAPSHOT] - 2026-04-14

### Added
- `AnthropicController` exposing `GET /api/anthropic/{message}` to call Claude via Spring AI
- Spring AI Anthropic starter (`spring-ai-starter-model-anthropic`) and BOM (`spring-ai-bom 2.0.0-M4`)
- `spring-boot-starter-web` for HTTP support
- `springboot4-dotenv` for `.env` file loading
- `ANTHROPIC_API_KEY` environment variable mapping in `application.properties`

### Initial
- Spring Boot 4.0.5 project setup with Java 25
- Maven wrapper (`mvnw`)
