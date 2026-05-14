package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.infra_shard.paging.CursorDirection;

import java.util.List;

public interface GetTickIndicatorSeriesPort {

    List<TickIndicatorView> findSeries(Long marketCodeId, String interval, String type, Long fromTs, Long toTs);

    List<TickIndicatorView> findCursor(Long marketCodeId, String interval, String type,
                                       Long cursor, int limit, CursorDirection direction);
}
