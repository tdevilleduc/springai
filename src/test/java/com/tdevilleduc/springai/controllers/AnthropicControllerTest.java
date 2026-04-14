package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnthropicControllerTest {

    @Mock
    private AnthropicChatModel chatModel;

    @Mock
    private RateLimitConfig rateLimitConfig;

    private Map<String, Bucket> rateLimitBuckets;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        rateLimitBuckets = new ConcurrentHashMap<>();
        AnthropicController controller = new AnthropicController(chatModel, rateLimitBuckets, rateLimitConfig);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAnswer_shouldReturnOkWhenUnderRateLimit() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call("bonjour")).thenReturn("Bonjour !");

        mockMvc.perform(get("/api/anthropic/bonjour"))
            .andExpect(status().isOk())
            .andExpect(content().string("Bonjour !"));
    }

    @Test
    void getAnswer_shouldReturn429WhenRateLimitExceeded() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(false);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);

        mockMvc.perform(get("/api/anthropic/bonjour"))
            .andExpect(status().isTooManyRequests());

        verify(chatModel, never()).call(anyString());
    }

    @Test
    void getAnswer_shouldReuseBucketForSameIp() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call(anyString())).thenReturn("ok");

        mockMvc.perform(get("/api/anthropic/hello"));
        mockMvc.perform(get("/api/anthropic/world"));

        // bucket created only once for the same IP
        verify(rateLimitConfig, times(1)).createBucket();
    }
}
