package com.tdevilleduc.springai.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    private final long cacheMaxSize;
    private final Duration cacheExpiry;

    public RateLimitConfig(
        @Value("${app.ratelimit.cache-max-size:100000}") long cacheMaxSize,
        @Value("${app.ratelimit.cache-expiry-hours:1}") long cacheExpiryHours
    ) {
        this.cacheMaxSize = cacheMaxSize;
        this.cacheExpiry = Duration.ofHours(cacheExpiryHours);
    }

    @Bean
    public Cache<String, Bucket> rateLimitBuckets() {
        return Caffeine.newBuilder()
            .maximumSize(cacheMaxSize)
            .expireAfterAccess(cacheExpiry)
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
