package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetTickIndicatorSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTickIndicatorSeriesUseCase {

    private final GetTickIndicatorSeriesPort port;

    public List<TickIndicatorView> execute(Long marketCodeId, String interval, String type, Long fromTs, Long toTs) {
        return port.findSeries(marketCodeId, interval, type, fromTs, toTs);
    }
}
