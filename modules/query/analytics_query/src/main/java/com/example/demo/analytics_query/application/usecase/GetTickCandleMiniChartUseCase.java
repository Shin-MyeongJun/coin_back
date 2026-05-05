package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleMiniChartPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTickCandleMiniChartUseCase {

    private final GetTickCandleMiniChartPort port;

    public List<TickCandleView> execute(Long marketCodeId, String interval, int limit) {
        return port.findTopN(marketCodeId, interval, limit);
    }
}
