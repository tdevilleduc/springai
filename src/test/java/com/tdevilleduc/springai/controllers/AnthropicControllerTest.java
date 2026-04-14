package com.tdevilleduc.springai.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnthropicControllerTest {

    @Mock
    private AnthropicChatModel chatModel;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AnthropicController controller = new AnthropicController(chatModel);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void chat_shouldReturnOkWithResponse() throws Exception {
        when(chatModel.call("bonjour")).thenReturn("Bonjour, comment puis-je vous aider ?");

        mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"bonjour\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("Bonjour, comment puis-je vous aider ?"));
    }

    @Test
    void chat_shouldReturnOkForAnyMessage() throws Exception {
        when(chatModel.call("test")).thenReturn("réponse test");

        mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"test\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("réponse test"));
    }

    @Test
    void chat_shouldHandleEmptyResponse() throws Exception {
        when(chatModel.call("hello")).thenReturn("");

        mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }
}
