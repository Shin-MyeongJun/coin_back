package com.example.demo.market_data_query.application.port.out;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.market_data_query.application.dto.PremiumDetailView;

import java.util.List;

public interface GetPremiumDetailRawPort {

    List<PremiumDetailView> findRaw(Long baseExchangeId, Long compareExchangeId, String symbol, Long fromTs, Long toTs);

    /**
     * Cursor 기반 조회. 결과는 timestamp 오름차순. UseCase 가 limit+1 을 전달하므로 hasMore 판정은 호출 측.
     */
    List<PremiumDetailView> findCursor(Long baseExchangeId, Long compareExchangeId, String symbol,
                                       Long cursor, int limit, CursorDirection direction);
}
