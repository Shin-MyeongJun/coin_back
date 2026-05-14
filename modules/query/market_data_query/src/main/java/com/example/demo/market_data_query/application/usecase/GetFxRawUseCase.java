package com.example.demo.market_data_query.application.usecase;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetFxRawPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFxRawUseCase {

    private final GetFxRawPort port;

    public List<FxView> execute(String baseCurrency, String quoteCurrency, Long fromTs, Long toTs) {
        return port.findRaw(baseCurrency, quoteCurrency, fromTs, toTs);
    }

    /**
     * Cursor 페이징 조회. {@code limit+1} 페치 후 hasMore 판정.
     */
    public CursorSlice<FxView> executeCursor(String baseCurrency, String quoteCurrency,
                                             Long cursor, int limit, CursorDirection direction) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<FxView> fetched = port.findCursor(baseCurrency, quoteCurrency, cursor, limit + 1, direction);
        boolean hasMore = fetched.size() > limit;
        List<FxView> items = hasMore ? fetched.subList(0, limit) : fetched;
        if (items.isEmpty()) {
            return CursorSlice.empty();
        }
        Long nextCursor = !hasMore ? null : switch (direction) {
            case BACKWARD -> items.get(0).timestamp() - 1L;
            case FORWARD -> items.get(items.size() - 1).timestamp() + 1L;
        };
        return new CursorSlice<>(List.copyOf(items), nextCursor, hasMore);
    }
}
