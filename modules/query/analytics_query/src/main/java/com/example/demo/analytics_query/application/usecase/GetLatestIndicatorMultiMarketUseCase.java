package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetLatestIndicatorMultiMarketPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLatestIndicatorMultiMarketUseCase {

    private final GetLatestIndicatorMultiMarketPort port;

    public List<TickIndicatorView> execute(List<Long> marketCodeIds, String interval, String type) {
        return port.findLatestForMarkets(marketCodeIds, interval, type);
    }
}
