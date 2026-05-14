package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.infra_shard.paging.CursorDirection;

import java.util.List;

public interface GetPremiumCandleDownsampledPort {

    List<PremiumCandleView> findDownsampled(String symbol, Long baseExchangeId, Long compareExchangeId,
                                            String sourceInterval, int targetBucketSeconds,
                                            Long fromTs, Long toTs);

    List<PremiumCandleView> findDownsampledCursor(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                  String sourceInterval, int targetBucketSeconds,
                                                  Long cursor, int limit, CursorDirection direction);
}
