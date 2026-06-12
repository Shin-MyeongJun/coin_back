package com.example.demo.alert.watchlist.application.usecase;

import com.example.demo.alert.watchlist.application.port.in.AddWatchlistItemUseCase;
import com.example.demo.alert.watchlist.application.port.in.RemoveWatchlistItemUseCase;
import com.example.demo.alert.watchlist.application.port.in.SearchWatchlistUseCase;
import com.example.demo.alert.watchlist.application.port.out.DeleteWatchlistPort;
import com.example.demo.alert.watchlist.application.port.out.LoadWatchlistPort;
import com.example.demo.alert.watchlist.application.port.out.SaveWatchlistPort;
import com.example.demo.alert.watchlist.domain.domain.WatchlistItem;
import com.example.demo.alert.watchlist.domain.exception.WatchlistItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class WatchlistService implements AddWatchlistItemUseCase, RemoveWatchlistItemUseCase, SearchWatchlistUseCase {
    private final LoadWatchlistPort loadWatchlistPort;
    private final SaveWatchlistPort saveWatchlistPort;
    private final DeleteWatchlistPort deleteWatchlistPort;
    private final Clock clock;

    @Override
    @Transactional
    public WatchlistItem add(String userId, AddWatchlistItemCommand command) {
        long now = clock.millis();
        WatchlistItem item = new WatchlistItem(
                null,
                userId,
                command.marketCodeId(),
                command.symbol(),
                command.domesticExchangeId(),
                command.offshoreExchangeId(),
                command.displayOrder() == null ? 0 : command.displayOrder(),
                command.memo(),
                now,
                now
        );
        return saveWatchlistPort.save(item);
    }

    @Override
    @Transactional
    public void remove(String userId, long id) {
        loadWatchlistPort.findByIdForUser(id, userId)
                .orElseThrow(() -> new WatchlistItemNotFoundException(id));
        deleteWatchlistPort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public WatchlistPage search(String userId, String keyword, int page, int size) {
        return loadWatchlistPort.searchByUser(userId, keyword, page, size);
    }
}
