package com.example.demo.market_data_query.application.usecase;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetFxDownsampledPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFxDownsampledUseCase {

    private final GetFxDownsampledPort port;

    public List<FxView> execute(String baseCurrency, String quoteCurrency, int bucketSeconds, Long fromTs, Long toTs) {
        return port.findDownsampled(baseCurrency, quoteCurrency, bucketSeconds, fromTs, toTs);
    }

    public CursorSlice<FxView> executeCursor(String baseCurrency, String quoteCurrency, int bucketSeconds,
                                             Long cursor, int limit, CursorDirection direction) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<FxView> fetched = port.findDownsampledCursor(
                baseCurrency, quoteCurrency, bucketSeconds, cursor, limit + 1, direction);
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
