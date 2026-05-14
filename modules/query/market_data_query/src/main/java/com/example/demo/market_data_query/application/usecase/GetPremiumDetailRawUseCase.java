package com.example.demo.market_data_query.application.usecase;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import com.example.demo.market_data_query.application.dto.PremiumDetailView;
import com.example.demo.market_data_query.application.port.out.GetPremiumDetailRawPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumDetailRawUseCase {

    private final GetPremiumDetailRawPort port;

    public List<PremiumDetailView> execute(Long baseExchangeId, Long compareExchangeId, String symbol,
                                           Long fromTs, Long toTs) {
        return port.findRaw(baseExchangeId, compareExchangeId, symbol, fromTs, toTs);
    }

    public CursorSlice<PremiumDetailView> executeCursor(Long baseExchangeId, Long compareExchangeId, String symbol,
                                                        Long cursor, int limit, CursorDirection direction) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<PremiumDetailView> fetched = port.findCursor(
                baseExchangeId, compareExchangeId, symbol, cursor, limit + 1, direction);
        boolean hasMore = fetched.size() > limit;
        List<PremiumDetailView> items = hasMore ? fetched.subList(0, limit) : fetched;
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
