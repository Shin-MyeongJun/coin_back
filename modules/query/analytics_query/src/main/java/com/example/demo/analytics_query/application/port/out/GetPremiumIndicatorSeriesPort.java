package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.infra_shard.paging.CursorDirection;

import java.util.List;

public interface GetPremiumIndicatorSeriesPort {

    List<PremiumIndicatorView> findSeries(String symbol, Long baseExchangeId, Long compareExchangeId,
                                          String interval, String type, Long fromTs, Long toTs);

    List<PremiumIndicatorView> findCursor(String symbol, Long baseExchangeId, Long compareExchangeId,
                                          String interval, String type,
                                          Long cursor, int limit, CursorDirection direction);
}
