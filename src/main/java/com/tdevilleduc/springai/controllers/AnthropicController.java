package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.config.RateLimitConfig;
import com.tdevilleduc.springai.dto.ChatRequest;
import com.tdevilleduc.springai.validation.PromptValidator;
import io.github.bucket4j.Bucket;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/anthropic")
public class AnthropicController {

    private static final Logger log = LoggerFactory.getLogger(AnthropicController.class);

    private final AnthropicChatModel chatModel;
    private final PromptValidator promptValidator;
    private final Map<String, Bucket> rateLimitBuckets;
    private final RateLimitConfig rateLimitConfig;
    private final MeterRegistry meterRegistry;

    public AnthropicController(AnthropicChatModel chatModel,
                               PromptValidator promptValidator,
                               Map<String, Bucket> rateLimitBuckets,
                               RateLimitConfig rateLimitConfig,
                               MeterRegistry meterRegistry) {
        this.chatModel = chatModel;
        this.promptValidator = promptValidator;
        this.rateLimitBuckets = rateLimitBuckets;
        this.rateLimitConfig = rateLimitConfig;
        this.meterRegistry = meterRegistry;

        Gauge.builder("ratelimit.buckets.size", rateLimitBuckets, Map::size)
            .description("Nombre de buckets IP actifs")
            .register(meterRegistry);
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request,
                                       HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        log.info("Requête reçue — ip={} messageLength={}", clientIp, request.message().length());

        promptValidator.validate(request.message());

        Bucket bucket = rateLimitBuckets.computeIfAbsent(clientIp, k -> rateLimitConfig.createBucket());

        if (!bucket.tryConsume(1)) {
            meterRegistry.counter("ratelimit.rejected", "ip", clientIp).increment();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Trop de requêtes. Limite : 10 requêtes par minute.");
        }

        String response = Timer.builder("anthropic.chat.duration")
            .description("Temps de réponse du modèle Anthropic")
            .register(meterRegistry)
            .record(() -> chatModel.call(request.message()));

        log.info("Réponse envoyée — ip={} responseLength={}", clientIp, response.length());
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
