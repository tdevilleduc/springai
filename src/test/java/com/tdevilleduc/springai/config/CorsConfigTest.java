package com.tdevilleduc.springai.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CorsConfigTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AnthropicChatModel chatModel;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void preflight_shouldReturnAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/v1/anthropic/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void preflight_shouldAllowGetMethod() throws Exception {
        mockMvc.perform(options("/api/v1/anthropic/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }

    @Test
    void preflight_shouldAllowPostMethod() throws Exception {
        mockMvc.perform(options("/api/v1/anthropic/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    void preflight_shouldAllowContentTypeHeader() throws Exception {
        mockMvc.perform(options("/api/v1/anthropic/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("Content-Type")));
    }

    @Test
    void preflight_shouldAllowAuthorizationHeader() throws Exception {
        mockMvc.perform(options("/api/v1/anthropic/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization"))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")));
    }

    @Test
    void preflight_shouldAllowXRequestedWithHeader() throws Exception {
        mockMvc.perform(options("/api/v1/anthropic/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "X-Requested-With"))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("X-Requested-With")));
    }

    @Test
    void preflight_shouldNotUseWildcardForAllowedHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/anthropic/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Content-Type"))
            .andExpect(header().string("Access-Control-Allow-Headers", not(containsString("*"))));
    }
}
