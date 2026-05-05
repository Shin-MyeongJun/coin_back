package com.example.demo.analytics_query.application.port.out;

import com.example.demo.analytics_query.application.dto.PremiumScreenerResult;
import com.example.demo.analytics_query.application.dto.ScreenerCondition;
import com.example.demo.analytics_query.application.dto.TickScreenerResult;

import java.util.List;

public interface GetScreenerPort {
    List<TickScreenerResult> findTickByConditions(List<ScreenerCondition> conditions);
    List<PremiumScreenerResult> findPremiumByConditions(List<ScreenerCondition> conditions);
}
