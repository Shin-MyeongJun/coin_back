package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.infra_shard.paging.CursorDirection;

import java.util.List;

public interface GetTickCandleSeriesPort {

    List<TickCandleView> findSeries(Long marketCodeId, String interval, Long fromTs, Long toTs);

    /**
     * Cursor 기반 조회. 결과는 bucketOpenTs 오름차순. UseCase 가 limit+1 을 전달한다.
     */
    List<TickCandleView> findCursor(Long marketCodeId, String interval,
                                    Long cursor, int limit, CursorDirection direction);
}
