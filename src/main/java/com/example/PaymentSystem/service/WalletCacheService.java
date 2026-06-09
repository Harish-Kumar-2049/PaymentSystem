package com.example.PaymentSystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "wallet:balance:";
    private static final Duration TTL = Duration.ofMinutes(5);

    public void cacheBalance(UUID walletId, BigDecimal balance) {
        redisTemplate.opsForValue()
                .set(PREFIX + walletId, balance.toString(), TTL);
    }

    public Optional<BigDecimal> getCachedBalance(UUID walletId) {
        Object cached = redisTemplate.opsForValue()
                .get(PREFIX + walletId);
        if (cached == null) return Optional.empty();
        return Optional.of(new BigDecimal(cached.toString()));
    }

    public void evictBalance(UUID walletId) {
        redisTemplate.delete(PREFIX + walletId);
        // call this after every debit or credit
    }
}

