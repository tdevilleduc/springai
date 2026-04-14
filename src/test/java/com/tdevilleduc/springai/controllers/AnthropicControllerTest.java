package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.validation.PromptValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnthropicControllerTest {

    @Mock
    private AnthropicChatModel chatModel;

    @Mock
    private PromptValidator promptValidator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AnthropicController controller = new AnthropicController(chatModel, promptValidator);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAnswer_shouldReturnOkWithResponse() throws Exception {
        when(chatModel.call("bonjour")).thenReturn("Bonjour, comment puis-je vous aider ?");

        mockMvc.perform(get("/api/anthropic/bonjour"))
            .andExpect(status().isOk())
            .andExpect(content().string("Bonjour, comment puis-je vous aider ?"));

        verify(promptValidator).validate("bonjour");
    }

    @Test
    void getAnswer_shouldReturnOkForAnyMessage() throws Exception {
        when(chatModel.call("test")).thenReturn("réponse test");

        mockMvc.perform(get("/api/anthropic/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("réponse test"));
    }

    @Test
    void getAnswer_shouldNotCallModelWhenValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("Le message contient du contenu non autorisé."))
            .when(promptValidator).validate(any());

        try {
            mockMvc.perform(get("/api/anthropic/badmessage"));
        } catch (Exception ignored) {
            // validation exception propagates as servlet exception in standalone setup
        }

        verify(chatModel, never()).call((String) any());
    }
}
