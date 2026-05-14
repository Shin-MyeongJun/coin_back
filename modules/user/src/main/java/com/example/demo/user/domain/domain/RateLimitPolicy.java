package com.example.demo.user.domain.domain;

public record RateLimitPolicy(int rpm, int rpd, int sseConcurrent) {

    public RateLimitPolicy {
        if (rpm <= 0) throw new IllegalArgumentException("rpm must be > 0");
        if (rpd <= 0) throw new IllegalArgumentException("rpd must be > 0");
        if (sseConcurrent < 0) throw new IllegalArgumentException("sseConcurrent must be >= 0");
    }
}
