package com.example.demo.user.domain.domain;

public final class RateLimitPolicies {

    private RateLimitPolicies() {}

    public static final RateLimitPolicy FREE  = new RateLimitPolicy(60,    10_000,    1);
    public static final RateLimitPolicy PRO   = new RateLimitPolicy(600,   200_000,   5);
    public static final RateLimitPolicy ADMIN = new RateLimitPolicy(6_000, 2_000_000, 20);

    public static RateLimitPolicy forTier(AccountTier tier) {
        return switch (tier) {
            case FREE  -> FREE;
            case PRO   -> PRO;
            case ADMIN -> ADMIN;
        };
    }
}
