package com.tdevilleduc.springai.config;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;

import static com.tdevilleduc.springai.config.RateLimitConfig.CACHE_EXPIRY;
import static com.tdevilleduc.springai.config.RateLimitConfig.CACHE_MAX_SIZE;
import static org.junit.jupiter.api.Assertions.*;

class RateLimitConfigTest {

    private final RateLimitConfig rateLimitConfig = new RateLimitConfig();

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
    void rateLimitBuckets_maxSizeShouldBe100000() {
        assertEquals(100_000, CACHE_MAX_SIZE);
    }

    @Test
    void rateLimitBuckets_expiryShouldBeOneHour() {
        assertEquals(3600, CACHE_EXPIRY.getSeconds());
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
