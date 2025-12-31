package com.example.DCRateLimiter.limiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenBucketLimiter implements RateLimiter{
    private final long capacity;
    private final double refillRatePerNanos;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final long ttlNanos = 60L * 1_000_000_000L;

    private final ScheduledExecutorService sweeper = Executors.newSingleThreadScheduledExecutor();

    public TokenBucketLimiter(long capacity, long refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerNanos = refillRatePerSecond / 1_000_000_000.0;

        sweeper.scheduleAtFixedRate(
                this::evictExpiredBuckets,
                30,
                30,
                TimeUnit.SECONDS
        );
    }

    @Override
    public boolean allowRequest(String key){
        long now = System.nanoTime();

        Bucket bucket = buckets.computeIfAbsent(
                key,
                k-> new Bucket(capacity)
        );

        synchronized (bucket){
            if(now - bucket.getLastAccessTime() > ttlNanos){
                buckets.remove(key);
                bucket = new Bucket(capacity);
                buckets.put(key, bucket);
            }

            refill(bucket, now);
            bucket.setLastAccessTime(System.nanoTime());

            if(bucket.getTokens() >= 1){
                bucket.setTokens(bucket.getTokens() - 1);
                return true;
            }
            return false;
        }
    }

    private void refill(Bucket bucket, long now){
        long elapsedNanos = now - bucket.getLastRefillTimestamp();

        if (elapsedNanos <= 0 ) return;

        double tokensToAdd = (elapsedNanos / 1_000_000_000.0) * refillRatePerNanos;
        double newTokenCount = Math.min(capacity, bucket.getTokens() + tokensToAdd);

        bucket.setTokens(newTokenCount);
        bucket.setLastRefillTimestamp(now);
    }

    private void evictExpiredBuckets(){
        long now = System.nanoTime();
        for(Map.Entry<String, Bucket> entry: buckets.entrySet()){
            Bucket bucket = entry.getValue();
            if(now - bucket.getLastAccessTime() > ttlNanos){
                buckets.remove(entry.getKey());

            }
        }
    }

//    private void evictExpiredBuckets(long ttlNanos){
//        long now = System.nanoTime();
//        buckets.entrySet().removeIf(entry -> (now - entry.getValue().getLastAccessTime()) > ttlNanos);
//
//    }

}
