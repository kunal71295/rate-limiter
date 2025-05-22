package com.example.rate_limiter.service;

public interface RateLimiter {

    boolean allowRequest(String userId);

}
