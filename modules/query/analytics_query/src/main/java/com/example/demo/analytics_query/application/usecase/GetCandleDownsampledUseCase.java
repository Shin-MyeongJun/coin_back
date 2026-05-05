package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.application.port.out.GetCandleDownsampledPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCandleDownsampledUseCase {

    private final GetCandleDownsampledPort port;

    public List<CandleView> execute(Long marketCodeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs) {
        return port.findDownsampledTickCandles(marketCodeId, sourceInterval, targetBucketSeconds, fromTs, toTs);
    }
}
