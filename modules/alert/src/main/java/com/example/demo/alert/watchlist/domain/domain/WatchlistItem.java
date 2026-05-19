package com.example.demo.alert.watchlist.domain.domain;

public record WatchlistItem(
        Long id,
        String userId,
        long marketCodeId,
        String symbol,
        Long domesticExchangeId,
        Long offshoreExchangeId,
        int displayOrder,
        String memo,
        long createdAt,
        long updatedAt
) {
    public WatchlistItem {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (marketCodeId <= 0) {
            throw new IllegalArgumentException("marketCodeId must be positive");
        }
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder must be non-negative");
        }
    }
}
