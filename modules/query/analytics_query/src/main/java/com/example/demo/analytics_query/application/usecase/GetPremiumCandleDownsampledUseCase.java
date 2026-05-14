package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleDownsampledPort;
import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumCandleDownsampledUseCase {

    private final GetPremiumCandleDownsampledPort port;

    public List<PremiumCandleView> execute(String symbol, Long baseExchangeId, Long compareExchangeId,
                                           String sourceInterval, int targetBucketSeconds,
                                           Long fromTs, Long toTs) {
        return port.findDownsampled(symbol, baseExchangeId, compareExchangeId,
                sourceInterval, targetBucketSeconds, fromTs, toTs);
    }

    public CursorSlice<PremiumCandleView> executeCursor(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                        String sourceInterval, int targetBucketSeconds,
                                                        Long cursor, int limit, CursorDirection direction) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<PremiumCandleView> fetched = port.findDownsampledCursor(
                symbol, baseExchangeId, compareExchangeId, sourceInterval, targetBucketSeconds,
                cursor, limit + 1, direction);
        boolean hasMore = fetched.size() > limit;
        List<PremiumCandleView> items = hasMore ? fetched.subList(0, limit) : fetched;
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
