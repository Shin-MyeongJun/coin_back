package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.CandleView;

import java.util.List;

public interface GetCandleDownsampledPort {
    List<CandleView> findDownsampledTickCandles(Long marketCodeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs);
}
