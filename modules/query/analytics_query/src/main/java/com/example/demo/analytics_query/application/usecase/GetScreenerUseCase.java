package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.ScreenerResult;
import com.example.demo.analytics_query.application.port.out.GetScreenerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetScreenerUseCase {

    private final GetScreenerPort port;

    public List<ScreenerResult> execute(String interval, String type, BigDecimal minValue, BigDecimal maxValue) {
        return port.findByIndicatorCondition(interval, type, minValue, maxValue);
    }
}
