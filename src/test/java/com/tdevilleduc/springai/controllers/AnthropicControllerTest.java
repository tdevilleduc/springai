package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.config.RateLimitConfig;
import com.tdevilleduc.springai.validation.PromptValidator;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnthropicControllerTest {

    @Mock
    private AnthropicChatModel chatModel;

    @Mock
    private PromptValidator promptValidator;

    @Mock
    private RateLimitConfig rateLimitConfig;

    private Map<String, Bucket> rateLimitBuckets;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        rateLimitBuckets = new ConcurrentHashMap<>();
        AnthropicController controller = new AnthropicController(chatModel, promptValidator, rateLimitBuckets, rateLimitConfig);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void chat_shouldReturnOkWithResponse() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call("bonjour")).thenReturn("Bonjour !");

        mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"bonjour\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("Bonjour !"));

        verify(promptValidator).validate("bonjour");
    }

    @Test
    void chat_shouldReturn429WhenRateLimitExceeded() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(false);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);

        mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"bonjour\"}"))
            .andExpect(status().isTooManyRequests());

        verify(chatModel, never()).call(anyString());
    }

    @Test
    void chat_shouldReuseBucketForSameIp() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call(anyString())).thenReturn("ok");

        mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"));
        mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"world\"}"));

        verify(rateLimitConfig, times(1)).createBucket();
    }

    @Test
    void chat_shouldNotCallModelWhenValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("Le message contient du contenu non autorisé."))
            .when(promptValidator).validate(any());

        try {
            mockMvc.perform(post("/api/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"badmessage\"}"));
        } catch (Exception ignored) {
            // validation exception propagates as servlet exception in standalone setup
        }

        verify(chatModel, never()).call((String) any());
    }
}
