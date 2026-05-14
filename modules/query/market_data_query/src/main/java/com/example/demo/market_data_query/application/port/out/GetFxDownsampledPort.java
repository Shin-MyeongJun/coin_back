package com.example.demo.market_data_query.application.port.out;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.market_data_query.application.dto.FxView;

import java.util.List;

public interface GetFxDownsampledPort {

    List<FxView> findDownsampled(String baseCurrency, String quoteCurrency,
                                 int bucketSeconds, Long fromTs, Long toTs);

    /**
     * Downsampled cursor 조회. 결과는 timestamp 오름차순. UseCase 가 limit+1 을 전달하므로 hasMore 판정은 호출 측.
     */
    List<FxView> findDownsampledCursor(String baseCurrency, String quoteCurrency, int bucketSeconds,
                                       Long cursor, int limit, CursorDirection direction);
}
