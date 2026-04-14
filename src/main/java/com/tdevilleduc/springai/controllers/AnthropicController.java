package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.config.RateLimitConfig;
import com.tdevilleduc.springai.dto.ChatRequest;
import com.tdevilleduc.springai.validation.PromptValidator;
import io.github.bucket4j.Bucket;
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
@RequestMapping("/api/anthropic")
public class AnthropicController {

    private static final Logger log = LoggerFactory.getLogger(AnthropicController.class);

    private final AnthropicChatModel chatModel;
    private final PromptValidator promptValidator;
    private final Map<String, Bucket> rateLimitBuckets;
    private final RateLimitConfig rateLimitConfig;

    public AnthropicController(AnthropicChatModel chatModel,
                               PromptValidator promptValidator,
                               Map<String, Bucket> rateLimitBuckets,
                               RateLimitConfig rateLimitConfig) {
        this.chatModel = chatModel;
        this.promptValidator = promptValidator;
        this.rateLimitBuckets = rateLimitBuckets;
        this.rateLimitConfig = rateLimitConfig;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request,
                                       HttpServletRequest httpRequest) {
        log.info("Requête reçue — ip={} messageLength={}", httpRequest.getRemoteAddr(), request.message().length());

        promptValidator.validate(request.message());

        String clientIp = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimitBuckets.computeIfAbsent(clientIp, k -> rateLimitConfig.createBucket());

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Trop de requêtes. Limite : 10 requêtes par minute.");
        }

        String response = chatModel.call(request.message());
        log.info("Réponse envoyée — ip={} responseLength={}", httpRequest.getRemoteAddr(), response.length());
        return ResponseEntity.ok(response);
    }
}
