package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.dto.ChatRequest;
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

    public AnthropicController(AnthropicChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        String response = chatModel.call(request.message());
        return ResponseEntity.ok(response);
    }
}
