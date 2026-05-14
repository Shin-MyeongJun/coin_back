package com.example.demo.user.domain.domain;

public record ApiKeyIssuanceQuota(int maxKeys) {

    public ApiKeyIssuanceQuota {
        if (maxKeys <= 0) throw new IllegalArgumentException("maxKeys must be > 0");
    }

    public static final ApiKeyIssuanceQuota FREE  = new ApiKeyIssuanceQuota(3);
    public static final ApiKeyIssuanceQuota PRO   = new ApiKeyIssuanceQuota(20);
    public static final ApiKeyIssuanceQuota ADMIN = new ApiKeyIssuanceQuota(50);

    public static ApiKeyIssuanceQuota forTier(AccountTier tier) {
        return switch (tier) {
            case FREE  -> FREE;
            case PRO   -> PRO;
            case ADMIN -> ADMIN;
        };
    }
}
