package com.example.demo.alert.watchlist.application.port.in;

public interface RemoveWatchlistItemUseCase {
    void remove(String userId, long id);
}
