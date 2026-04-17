package com.tdevilleduc.springai.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {

    private final int maxAttempts;
    private final long blockDurationSeconds;

    private record AttemptInfo(int count, Instant lastAttempt) {}

    private final ConcurrentHashMap<String, AttemptInfo> cache = new ConcurrentHashMap<>();

    public BruteForceProtectionService(
            @Value("${app.security.max-auth-attempts:5}") int maxAttempts,
            @Value("${app.security.block-duration-minutes:15}") int blockDurationMinutes) {
        this.maxAttempts = maxAttempts;
        this.blockDurationSeconds = (long) blockDurationMinutes * 60;
    }

    public void recordFailure(String ip) {
        cache.merge(ip,
            new AttemptInfo(1, Instant.now()),
            (existing, ignored) -> new AttemptInfo(existing.count() + 1, Instant.now()));
    }

    public boolean isBlocked(String ip) {
        AttemptInfo info = cache.get(ip);
        if (info == null || info.count() < maxAttempts) return false;
        return info.lastAttempt().plusSeconds(blockDurationSeconds).isAfter(Instant.now());
    }

    public void resetFailures(String ip) {
        cache.remove(ip);
    }

    int getFailureCount(String ip) {
        AttemptInfo info = cache.get(ip);
        return info == null ? 0 : info.count();
    }
}
