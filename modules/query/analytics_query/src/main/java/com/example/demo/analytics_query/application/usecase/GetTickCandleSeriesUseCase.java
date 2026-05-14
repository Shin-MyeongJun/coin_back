package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleSeriesPort;
import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTickCandleSeriesUseCase {

    private final GetTickCandleSeriesPort port;

    public List<TickCandleView> execute(Long marketCodeId, String interval, Long fromTs, Long toTs) {
        return port.findSeries(marketCodeId, interval, fromTs, toTs);
    }

    public CursorSlice<TickCandleView> executeCursor(Long marketCodeId, String interval,
                                                     Long cursor, int limit, CursorDirection direction) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<TickCandleView> fetched = port.findCursor(marketCodeId, interval, cursor, limit + 1, direction);
        boolean hasMore = fetched.size() > limit;
        List<TickCandleView> items = hasMore ? fetched.subList(0, limit) : fetched;
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
