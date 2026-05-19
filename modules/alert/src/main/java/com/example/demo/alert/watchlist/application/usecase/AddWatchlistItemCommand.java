package com.example.demo.alert.watchlist.application.usecase;

public record AddWatchlistItemCommand(
        long marketCodeId,
        String symbol,
        Long domesticExchangeId,
        Long offshoreExchangeId,
        Integer displayOrder,
        String memo
) {
}
