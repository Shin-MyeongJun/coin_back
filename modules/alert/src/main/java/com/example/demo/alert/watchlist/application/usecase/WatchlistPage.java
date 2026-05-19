package com.example.demo.alert.watchlist.application.usecase;

import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;

import java.util.List;

public record WatchlistPage(
        List<WatchlistItem> items,
        int page,
        int size,
        long total
) {
}
