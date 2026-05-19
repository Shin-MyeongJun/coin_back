package com.example.demo.alert.watchlist.application.port.in;

import com.example.demo.alert.watchlist.application.usecase.WatchlistPage;

public interface SearchWatchlistUseCase {
    WatchlistPage search(String userId, String keyword, int page, int size);
}
