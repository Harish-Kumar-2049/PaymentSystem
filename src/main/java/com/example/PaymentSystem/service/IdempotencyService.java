package com.example.PaymentSystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    // returns true if this is a NEW request (safe to process)
    // returns false if this key was already processed (duplicate)
    public boolean isNewRequest(String idempotencyKey) {
        String redisKey = PREFIX + idempotencyKey;
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", TTL);
        return Boolean.TRUE.equals(isNew);
    }

    public void markCompleted(String idempotencyKey) {
        String redisKey = PREFIX + idempotencyKey;
        redisTemplate.opsForValue()
                .set(redisKey, "COMPLETED", TTL);
    }

    public void markFailed(String idempotencyKey) {
        String redisKey = PREFIX + idempotencyKey;
        redisTemplate.delete(redisKey);
        // delete on failure so client can retry
    }
}

