package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/anthropic")
public class AnthropicController {

    private final AnthropicChatModel chatModel;
    private final Map<String, Bucket> rateLimitBuckets;
    private final RateLimitConfig rateLimitConfig;

    public AnthropicController(AnthropicChatModel chatModel,
                               Map<String, Bucket> rateLimitBuckets,
                               RateLimitConfig rateLimitConfig) {
        this.chatModel = chatModel;
        this.rateLimitBuckets = rateLimitBuckets;
        this.rateLimitConfig = rateLimitConfig;
    }

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message,
                                            HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        Bucket bucket = rateLimitBuckets.computeIfAbsent(clientIp, k -> rateLimitConfig.createBucket());

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Trop de requêtes. Limite : 10 requêtes par minute.");
        }

        String response = chatModel.call(message);
        return ResponseEntity.ok(response);
    }
}
