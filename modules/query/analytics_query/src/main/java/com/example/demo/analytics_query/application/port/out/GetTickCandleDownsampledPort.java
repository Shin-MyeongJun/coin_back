package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.infra_shard.paging.CursorDirection;

import java.util.List;

public interface GetTickCandleDownsampledPort {

    List<TickCandleView> findDownsampled(Long marketCodeId, String sourceInterval,
                                         int targetBucketSeconds, Long fromTs, Long toTs);

    List<TickCandleView> findDownsampledCursor(Long marketCodeId, String sourceInterval, int targetBucketSeconds,
                                               Long cursor, int limit, CursorDirection direction);
}
