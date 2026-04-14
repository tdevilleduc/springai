package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.dto.ChatRequest;
import com.tdevilleduc.springai.validation.PromptValidator;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        promptValidator.validate(request.message());
        String response = chatModel.call(request.message());
        return ResponseEntity.ok(response);
    }
}
