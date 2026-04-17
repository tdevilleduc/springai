package com.tdevilleduc.springai.security;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BruteForceAuthenticationEntryPointTest {

    private static final int MAX_ATTEMPTS = 3;
    private BruteForceProtectionService service;
    private BruteForceAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        service = new BruteForceProtectionService(MAX_ATTEMPTS, 15);
        entryPoint = new BruteForceAuthenticationEntryPoint(service, new SimpleMeterRegistry());
    }

    @Test
    void noCreds_shouldReturn401WithoutIncrementingCounter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("no creds"));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(0, service.getFailureCount(request.getRemoteAddr()));
    }

    @Test
    void badCredentials_shouldReturn401AndIncrementCounter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad creds"));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(1, service.getFailureCount(request.getRemoteAddr()));
    }

    @Test
    void afterMaxAttempts_shouldReturn429() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            entryPoint.commence(request, response, new BadCredentialsException("bad creds"));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        }

        MockHttpServletResponse lastResponse = new MockHttpServletResponse();
        entryPoint.commence(request, lastResponse, new BadCredentialsException("bad creds"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), lastResponse.getStatus());
    }

    @Test
    void blockedIp_xForwardedFor_shouldReturn429() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            entryPoint.commence(request, response, new BadCredentialsException("bad creds"));
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new BadCredentialsException("bad creds"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatus());
    }

    @Test
    void response401_shouldContainWwwAuthenticateHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad creds"));

        assertTrue(response.getHeader("WWW-Authenticate").contains("Basic"));
    }
}
