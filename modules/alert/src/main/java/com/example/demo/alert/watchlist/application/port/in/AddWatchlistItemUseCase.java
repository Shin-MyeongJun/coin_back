package com.example.demo.alert.watchlist.application.port.in;

import com.example.demo.alert.watchlist.application.usecase.AddWatchlistItemCommand;
import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;

public interface AddWatchlistItemUseCase {
    WatchlistItem add(String userId, AddWatchlistItemCommand command);
}
