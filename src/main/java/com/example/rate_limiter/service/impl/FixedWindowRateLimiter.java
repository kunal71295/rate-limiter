package com.example.rate_limiter.service.impl;

import com.example.rate_limiter.service.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${rate.limit.max.requests}")
    private long RATE_LIMIT_MAX_REQUESTS;

    @Override
    public boolean allowRequest(String userId) {
        String key = getKey(userId);
        long count = Objects.requireNonNull(redisTemplate.opsForValue().increment(key));
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        return count <= RATE_LIMIT_MAX_REQUESTS;
    }

    private String getKey(String userId) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        return "rate-limit:" + userId + ":" + currentMinute;
    }
}
