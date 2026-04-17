package com.tdevilleduc.springai.security;

import com.tdevilleduc.springai.util.IpUtils;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BruteForceAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(BruteForceAuthenticationEntryPoint.class);

    private final BruteForceProtectionService bruteForceProtectionService;
    private final MeterRegistry meterRegistry;

    public BruteForceAuthenticationEntryPoint(BruteForceProtectionService bruteForceProtectionService,
                                               MeterRegistry meterRegistry) {
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String ip = IpUtils.getClientIp(request);

        if (authException instanceof BadCredentialsException) {
            bruteForceProtectionService.recordFailure(ip);
            meterRegistry.counter("security.auth.failure", "ip", ip).increment();

            if (bruteForceProtectionService.isBlocked(ip)) {
                log.warn("Brute force détecté — ip={} bloquée", ip);
                meterRegistry.counter("security.bruteforce.blocked", "ip", ip).increment();
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.getWriter().write("Trop de tentatives de connexion. Réessayez dans quelques minutes.");
                return;
            }

            log.warn("Échec d'authentification — ip={} tentatives={}", ip,
                bruteForceProtectionService.getFailureCount(ip));
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("WWW-Authenticate", "Basic realm=\"springai\"");
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write("Authentification requise.");
    }
}
