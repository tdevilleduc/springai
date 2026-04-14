package com.tdevilleduc.springai.config;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitConfigTest {

    private final RateLimitConfig rateLimitConfig = new RateLimitConfig();

    @Test
    void rateLimitBuckets_shouldReturnEmptyMap() {
        Map<String, Bucket> buckets = rateLimitConfig.rateLimitBuckets();
        assertNotNull(buckets);
        assertTrue(buckets.isEmpty());
    }

    @Test
    void createBucket_shouldAllowTenConsecutiveRequests() {
        Bucket bucket = rateLimitConfig.createBucket();

        for (int i = 0; i < 10; i++) {
            assertTrue(bucket.tryConsume(1), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void createBucket_shouldRejectEleventhRequest() {
        Bucket bucket = rateLimitConfig.createBucket();

        for (int i = 0; i < 10; i++) {
            bucket.tryConsume(1);
        }

        assertFalse(bucket.tryConsume(1), "11th request should be rejected");
    }

    @Test
    void createBucket_shouldReturnNonNullBucket() {
        assertNotNull(rateLimitConfig.createBucket());
    }
}
