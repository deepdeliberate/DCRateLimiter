package com.example.DCRateLimiter.limiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketLimiter {
    private final long capacity;
    private final double refillRatePerNanos;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketLimiter(long capacity, long refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerNanos = refillRatePerSecond / 1_000_000_000.0;
    }

    public boolean allowRequest(String key){
        Bucket bucket = buckets.computeIfAbsent(
                key,
                k-> new Bucket(capacity)
        );

        synchronized (bucket){
            refill(bucket);

            if(bucket.getTokens() >= 1){
                bucket.setTokens(bucket.getTokens() - 1);
                return true;
            }
            return false;
        }
    }

    private void refill(Bucket bucket){
        long now = System.nanoTime();
        long elapsedNanos = now - bucket.getLastRefillTimestamp();

        if (elapsedNanos <= 0 ) return;

        double tokensToAdd = elapsedNanos * refillRatePerNanos;
        double newTokenCount = Math.min(capacity, bucket.getTokens() + tokensToAdd);

        bucket.setTokens(newTokenCount);
        bucket.setLastRefillTimestamp(now);
    }
}
