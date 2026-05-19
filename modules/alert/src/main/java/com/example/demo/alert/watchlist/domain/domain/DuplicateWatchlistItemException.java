package com.example.demo.alert.watchlist.domain.domain;

public class DuplicateWatchlistItemException extends RuntimeException {
    public DuplicateWatchlistItemException(String message, Throwable cause) {
        super(message, cause);
    }
}
