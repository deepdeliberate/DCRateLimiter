package com.example.DCRateLimiter.limiter;

public interface RateLimiter {
    boolean allowRequest(String key);
}
