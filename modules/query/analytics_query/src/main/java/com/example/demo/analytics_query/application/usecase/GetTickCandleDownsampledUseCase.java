package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleDownsampledPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTickCandleDownsampledUseCase {

    private final GetTickCandleDownsampledPort port;

    public List<TickCandleView> execute(Long marketCodeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs) {
        return port.findDownsampled(marketCodeId, sourceInterval, targetBucketSeconds, fromTs, toTs);
    }
}
