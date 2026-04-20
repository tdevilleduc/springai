package com.tdevilleduc.springai.config;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitConfigTest {

    private final RateLimitConfig rateLimitConfig = new RateLimitConfig(100_000, 1);

    @Test
    void rateLimitBuckets_shouldReturnNonNullCache() {
        Cache<String, Bucket> cache = rateLimitConfig.rateLimitBuckets();
        assertNotNull(cache);
    }

    @Test
    void rateLimitBuckets_shouldBeEmptyInitially() {
        Cache<String, Bucket> cache = rateLimitConfig.rateLimitBuckets();
        assertEquals(0, cache.estimatedSize());
    }

    @Test
    void rateLimitBuckets_shouldRespectConfiguredMaxSize() {
        RateLimitConfig config = new RateLimitConfig(500, 1);
        Cache<String, Bucket> cache = config.rateLimitBuckets();
        cache.policy().eviction().ifPresent(eviction ->
            assertEquals(500, eviction.getMaximum())
        );
    }

    @Test
    void rateLimitBuckets_shouldRespectConfiguredExpiryHours() {
        RateLimitConfig config = new RateLimitConfig(100_000, 2);
        Cache<String, Bucket> cache = config.rateLimitBuckets();
        cache.policy().expireAfterAccess().ifPresent(expiry ->
            assertEquals(7200, expiry.getExpiresAfter().toSeconds())
        );
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
