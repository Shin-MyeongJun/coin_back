package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumScreenerResult;
import com.example.demo.analytics_query.application.dto.ScreenerCondition;
import com.example.demo.analytics_query.application.dto.TickScreenerResult;
import com.example.demo.analytics_query.application.port.out.GetScreenerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetScreenerUseCase {

    private final GetScreenerPort port;

    public List<TickScreenerResult> executeForTick(List<ScreenerCondition> conditions) {
        return port.findTickByConditions(conditions);
    }

    public List<PremiumScreenerResult> executeForPremium(List<ScreenerCondition> conditions) {
        return port.findPremiumByConditions(conditions);
    }
}
