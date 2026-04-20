package com.tdevilleduc.springai.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    static final long CACHE_MAX_SIZE = 100_000;
    static final Duration CACHE_EXPIRY = Duration.ofHours(1);

    @Bean
    public Cache<String, Bucket> rateLimitBuckets() {
        return Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .expireAfterAccess(CACHE_EXPIRY)
            .build();
    }

    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(10)
            .refillIntervally(10, Duration.ofMinutes(1))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
