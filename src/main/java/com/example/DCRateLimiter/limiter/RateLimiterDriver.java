package com.example.DCRateLimiter.limiter;


public class RateLimiterDriver {
    public static void main(String[] args) throws InterruptedException {
        TokenBucketLimiter limiter = new TokenBucketLimiter(
                5,
                1
        );

        String key = "user-1";

        System.out.println("Sending 3 requests");

        for(int i = 1; i <= 3; i++){
            boolean allowed = limiter.allowRequest(key);
            System.out.printf(
                    "Request %d -> %s%n",
                    i,
                    allowed ? "ALLOWED" : "BLOCKED"
            );
            Thread.sleep(500);
        }

        System.out.println("\n Idle (wait for TTL)");
        System.out.println("Waiting 70s");
        Thread.sleep(70_000);

        System.out.println("Send Request Again");
        boolean allowed = limiter.allowRequest(key);

        System.out.println(
                "Request after TTL -> " +
                        (allowed ? "ALLOWED" : "BLOCKED")
        );
    }
}
