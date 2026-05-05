package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.TickCandleView;

import java.util.List;

public interface GetTickCandleDownsampledPort {
    List<TickCandleView> findDownsampled(Long marketCodeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs);
}
