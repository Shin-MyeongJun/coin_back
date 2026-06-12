package com.example.demo.alert.watchlist.domain.exception;

/** Watchlist item 미조회 예외. 미존재/비소유 비구분 — 404 단일 응답. */
public class WatchlistItemNotFoundException extends RuntimeException {

    public WatchlistItemNotFoundException(long watchlistItemId) {
        super("watchlist item not found: " + watchlistItemId);
    }
}
