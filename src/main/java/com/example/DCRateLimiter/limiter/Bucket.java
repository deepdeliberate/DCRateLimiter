package com.example.DCRateLimiter.limiter;

public class Bucket {
    private double tokens;
    private long lastRefillTimestamp;
    private long lastAccessTime;

    public Bucket(double capacity){
        this.tokens = capacity;
        this.lastRefillTimestamp = System.nanoTime();
        this.lastAccessTime = System.nanoTime();
    }

    public double getTokens() {
        return tokens;
    }
    public void setTokens(double tokens){
        this.tokens = tokens;
    }

    public long getLastRefillTimestamp() {
        return this.lastRefillTimestamp;
    }
    public void setLastRefillTimestamp(long lastRefillTimestamp){
        this.lastRefillTimestamp = lastRefillTimestamp;
    }

    public long getLastAccessTime(){ return lastAccessTime; }
    public void setLastAccessTime(long lastAccessTime){ this.lastAccessTime = lastAccessTime;}

}
