package com.tdevilleduc.springai.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/anthropic")
public class AnthropicController {

    private static final Logger log = LoggerFactory.getLogger(AnthropicController.class);

    private final AnthropicChatModel chatModel;

    public AnthropicController(AnthropicChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message,
                                            HttpServletRequest request) {
        log.info("Requête reçue — ip={} messageLength={}", request.getRemoteAddr(), message.length());
        String response = chatModel.call(message);
        log.info("Réponse envoyée — ip={} responseLength={}", request.getRemoteAddr(), response.length());
        return ResponseEntity.ok(response);
    }
}
