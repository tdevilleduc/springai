package com.tdevilleduc.springai.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AnthropicChatModel chatModel;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void unauthenticatedRequest_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedRequest_shouldReturn200() throws Exception {
        when(chatModel.call("hello")).thenReturn("réponse");

        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(APPLICATION_JSON)
                .content("{\"message\":\"hello\"}")
                .with(httpBasic("testuser", "testpass")))
            .andExpect(status().isOk());
    }

    @Test
    void wrongPassword_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(APPLICATION_JSON)
                .content("{\"message\":\"hello\"}")
                .with(httpBasic("testuser", "wrongpass")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordEncoder_shouldBeBCrypt() {
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
    }

    @Test
    void passwordEncoder_shouldEncodeAndMatch() {
        String raw = "mypassword";
        String encoded = passwordEncoder.encode(raw);
        assertTrue(passwordEncoder.matches(raw, encoded));
    }

    @Test
    void response_shouldContainXContentTypeOptionsHeader() throws Exception {
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(APPLICATION_JSON)
                .content("{\"message\":\"hello\"}")
                .with(httpBasic("testuser", "testpass")))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void response_shouldContainXFrameOptionsDeny() throws Exception {
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(APPLICATION_JSON)
                .content("{\"message\":\"hello\"}")
                .with(httpBasic("testuser", "testpass")))
            .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void response_shouldContainContentSecurityPolicy() throws Exception {
        mockMvc.perform(post("/api/v1/anthropic/chat")
                .contentType(APPLICATION_JSON)
                .content("{\"message\":\"hello\"}")
                .with(httpBasic("testuser", "testpass")))
            .andExpect(header().string("Content-Security-Policy", "default-src 'self'"));
    }
}
