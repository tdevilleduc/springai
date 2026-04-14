package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.validation.PromptValidator;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/anthropic")
public class AnthropicController {

    private final AnthropicChatModel chatModel;
    private final PromptValidator promptValidator;

    public AnthropicController(AnthropicChatModel chatModel, PromptValidator promptValidator) {
        this.chatModel = chatModel;
        this.promptValidator = promptValidator;
    }

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message) {
        promptValidator.validate(message);
        String response = chatModel.call(message);
        return ResponseEntity.ok(response);
    }
}
