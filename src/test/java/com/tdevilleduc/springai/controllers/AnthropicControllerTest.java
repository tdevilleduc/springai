package com.tdevilleduc.springai.controllers;

import com.tdevilleduc.springai.config.RateLimitConfig;
import com.tdevilleduc.springai.validation.PromptValidator;
import io.github.bucket4j.Bucket;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tdevilleduc.springai.exception.GlobalExceptionHandler;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private Cache<String, Bucket> rateLimitBuckets;
    private MeterRegistry meterRegistry;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        rateLimitBuckets = Caffeine.newBuilder().build();
        meterRegistry = new SimpleMeterRegistry();
        AnthropicController controller = new AnthropicController(
            chatModel, promptValidator, rateLimitBuckets, rateLimitConfig, meterRegistry);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @Test
    void chat_shouldReturnOkWithResponse() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call("bonjour")).thenReturn("Bonjour !");

        mockMvc.perform(post("/api/v1/anthropic/chat")
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

        mockMvc.perform(post("/api/v1/anthropic/chat")
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

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"));
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"world\"}"));

        verify(rateLimitConfig, times(1)).createBucket();
    }

    @Test
    void chat_shouldNotCallModelWhenValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("Le message contient du contenu non autorisé."))
            .when(promptValidator).validate(any());

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"badmessage\"}"))
            .andExpect(status().isBadRequest());

        verify(chatModel, never()).call((String) any());
    }

    @Test
    void chat_shouldReturn400WhenMessageIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":null}"))
            .andExpect(status().isBadRequest());

        verify(chatModel, never()).call((String) any());
    }

    @Test
    void chat_shouldReturn400WhenMessageIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"\"}"))
            .andExpect(status().isBadRequest());

        verify(chatModel, never()).call((String) any());
    }

    @Test
    void chat_shouldReturn400WhenMessageExceedsMaxSize() throws Exception {
        String oversized = "a".repeat(4001);
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + oversized + "\"}"))
            .andExpect(status().isBadRequest());

        verify(chatModel, never()).call((String) any());
    }

    @Test
    void chat_shouldUseXForwardedForIpForRateLimiting() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call(anyString())).thenReturn("ok");

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .header("X-Forwarded-For", "203.0.113.42")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk());

        verify(rateLimitConfig, times(1)).createBucket();
        assert rateLimitBuckets.asMap().containsKey("203.0.113.42");
    }

    @Test
    void chat_shouldUseFirstIpFromXForwardedForChain() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call(anyString())).thenReturn("ok");

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .header("X-Forwarded-For", "203.0.113.42, 10.0.0.1, 192.168.1.1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk());

        assert rateLimitBuckets.asMap().containsKey("203.0.113.42");
        assert !rateLimitBuckets.asMap().containsKey("10.0.0.1");
    }

    @Test
    void chat_shouldFallbackToRemoteAddrWhenNoXForwardedFor() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call(anyString())).thenReturn("ok");

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk());

        // MockMvc uses 127.0.0.1 as default remote address
        assert rateLimitBuckets.asMap().containsKey("127.0.0.1");
    }

    @Test
    void chat_shouldRecordAnthropicCallDuration() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(true);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);
        when(chatModel.call(anyString())).thenReturn("ok");

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk());

        assertNotNull(meterRegistry.find("anthropic.chat.duration").timer(),
            "Le timer anthropic.chat.duration doit être enregistré");
        assertEquals(1, meterRegistry.find("anthropic.chat.duration").timer().count(),
            "Le timer doit avoir enregistré 1 appel");
    }

    @Test
    void chat_shouldIncrementRateLimitCounterWhenExceeded() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucket.tryConsume(1)).thenReturn(false);
        when(rateLimitConfig.createBucket()).thenReturn(bucket);

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isTooManyRequests());

        assertNotNull(meterRegistry.find("ratelimit.rejected").counter(),
            "Le counter ratelimit.rejected doit être enregistré");
        assertEquals(1.0, meterRegistry.find("ratelimit.rejected").counter().count(),
            "Le counter doit valoir 1 après un rejet");
    }

    @Test
    void constructor_shouldRegisterBucketSizeGauge() {
        assertNotNull(meterRegistry.find("ratelimit.buckets.size").gauge(),
            "La gauge ratelimit.buckets.size doit être enregistrée");
        assertEquals(0.0, meterRegistry.find("ratelimit.buckets.size").gauge().value(),
            "La gauge doit valoir 0 avec un map vide");
    }
}
