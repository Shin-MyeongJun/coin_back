package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetPremiumIndicatorSeriesPort;
import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumIndicatorSeriesUseCase {

    private final GetPremiumIndicatorSeriesPort port;

    public List<PremiumIndicatorView> execute(String symbol, Long baseExchangeId, Long compareExchangeId,
                                              String interval, String type, Long fromTs, Long toTs) {
        return port.findSeries(symbol, baseExchangeId, compareExchangeId, interval, type, fromTs, toTs);
    }

    public CursorSlice<PremiumIndicatorView> executeCursor(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                           String interval, String type,
                                                           Long cursor, int limit, CursorDirection direction) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<PremiumIndicatorView> fetched = port.findCursor(
                symbol, baseExchangeId, compareExchangeId, interval, type, cursor, limit + 1, direction);
        boolean hasMore = fetched.size() > limit;
        List<PremiumIndicatorView> items = hasMore ? fetched.subList(0, limit) : fetched;
        if (items.isEmpty()) {
            return CursorSlice.empty();
        }
        Long nextCursor = !hasMore ? null : switch (direction) {
            case BACKWARD -> items.get(0).bucketOpenTs() - 1L;
            case FORWARD -> items.get(items.size() - 1).bucketOpenTs() + 1L;
        };
        return new CursorSlice<>(List.copyOf(items), nextCursor, hasMore);
    }
}
