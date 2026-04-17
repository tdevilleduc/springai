package com.tdevilleduc.springai.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceProtectionServiceTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_DURATION_MINUTES = 15;
    private static final String IP = "192.168.1.1";

    private BruteForceProtectionService service;

    @BeforeEach
    void setUp() {
        service = new BruteForceProtectionService(MAX_ATTEMPTS, BLOCK_DURATION_MINUTES);
    }

    @Test
    void newIp_shouldNotBeBlocked() {
        assertFalse(service.isBlocked(IP));
    }

    @Test
    void belowMaxAttempts_shouldNotBeBlocked() {
        service.recordFailure(IP);
        service.recordFailure(IP);
        assertFalse(service.isBlocked(IP));
    }

    @Test
    void atMaxAttempts_shouldBeBlocked() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.recordFailure(IP);
        }
        assertTrue(service.isBlocked(IP));
    }

    @Test
    void afterReset_shouldNotBeBlocked() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.recordFailure(IP);
        }
        assertTrue(service.isBlocked(IP));
        service.resetFailures(IP);
        assertFalse(service.isBlocked(IP));
    }

    @Test
    void getFailureCount_shouldReflectRecordedFailures() {
        assertEquals(0, service.getFailureCount(IP));
        service.recordFailure(IP);
        service.recordFailure(IP);
        assertEquals(2, service.getFailureCount(IP));
    }

    @Test
    void differentIps_shouldBeTrackedIndependently() {
        String otherIp = "10.0.0.1";
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.recordFailure(IP);
        }
        assertTrue(service.isBlocked(IP));
        assertFalse(service.isBlocked(otherIp));
    }
}
