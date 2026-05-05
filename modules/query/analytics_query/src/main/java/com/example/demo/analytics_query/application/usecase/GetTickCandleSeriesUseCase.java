package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTickCandleSeriesUseCase {

    private final GetTickCandleSeriesPort port;

    public List<TickCandleView> execute(Long marketCodeId, String interval, Long fromTs, Long toTs) {
        return port.findSeries(marketCodeId, interval, fromTs, toTs);
    }
}
