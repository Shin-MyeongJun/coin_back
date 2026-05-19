package com.example.demo.alert.watchlist.application.port.out;

import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;

public interface SaveWatchlistPort {
    WatchlistItem save(WatchlistItem item);
}
