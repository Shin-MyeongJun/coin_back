package com.example.demo.api.controller.watchlist.dto;

import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;

public record WatchlistItemResponse(
        Long id,
        long marketCodeId,
        String symbol,
        Long domesticExchangeId,
        Long offshoreExchangeId,
        int displayOrder,
        String memo,
        long createdAt,
        long updatedAt
) {
    public static WatchlistItemResponse from(WatchlistItem item) {
        return new WatchlistItemResponse(
                item.id(),
                item.marketCodeId(),
                item.symbol(),
                item.domesticExchangeId(),
                item.offshoreExchangeId(),
                item.displayOrder(),
                item.memo(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
