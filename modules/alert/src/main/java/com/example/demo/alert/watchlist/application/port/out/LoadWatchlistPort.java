package com.example.demo.alert.watchlist.application.port.out;

import com.example.demo.alert.watchlist.application.usecase.WatchlistPage;
import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;

import java.util.Optional;

public interface LoadWatchlistPort {
    Optional<WatchlistItem> findByIdForUser(long id, String userId);

    WatchlistPage searchByUser(String userId, String keyword, int page, int size);
}
