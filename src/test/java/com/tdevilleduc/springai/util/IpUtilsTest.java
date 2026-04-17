package com.tdevilleduc.springai.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpUtilsTest {

    @Test
    void noXForwardedFor_shouldReturnRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");

        assertEquals("10.0.0.1", IpUtils.getClientIp(request));
    }

    @Test
    void xForwardedForSingleIp_shouldReturnIt() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertEquals("1.2.3.4", IpUtils.getClientIp(request));
    }

    @Test
    void xForwardedForMultipleIps_shouldReturnFirst() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8, 9.10.11.12");

        assertEquals("1.2.3.4", IpUtils.getClientIp(request));
    }

    @Test
    void xForwardedForBlank_shouldFallbackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "   ");
        request.setRemoteAddr("10.0.0.1");

        assertEquals("10.0.0.1", IpUtils.getClientIp(request));
    }
}
