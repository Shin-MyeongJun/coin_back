package com.example.demo.market_data_query.application.usecase;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;
import com.example.demo.market_data_query.application.port.out.GetPremiumDetailAggPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumDetailAggUseCase {

    private final GetPremiumDetailAggPort port;

    public List<PremiumTimeSeriesView> execute(Long baseExchangeId, Long compareExchangeId, String symbol,
                                               int bucketSeconds, Long fromTs, Long toTs) {
        return port.findAgg(baseExchangeId, compareExchangeId, symbol, bucketSeconds, fromTs, toTs);
    }

    public CursorSlice<PremiumTimeSeriesView> executeCursor(Long baseExchangeId, Long compareExchangeId, String symbol,
                                                            int bucketSeconds, Long cursor, int limit,
                                                            CursorDirection direction) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<PremiumTimeSeriesView> fetched = port.findAggCursor(
                baseExchangeId, compareExchangeId, symbol, bucketSeconds, cursor, limit + 1, direction);
        boolean hasMore = fetched.size() > limit;
        List<PremiumTimeSeriesView> items = hasMore ? fetched.subList(0, limit) : fetched;
        if (items.isEmpty()) {
            return CursorSlice.empty();
        }
        Long nextCursor = !hasMore ? null : switch (direction) {
            case BACKWARD -> items.get(0).bucketTs() - 1L;
            case FORWARD -> items.get(items.size() - 1).bucketTs() + 1L;
        };
        return new CursorSlice<>(List.copyOf(items), nextCursor, hasMore);
    }
}
