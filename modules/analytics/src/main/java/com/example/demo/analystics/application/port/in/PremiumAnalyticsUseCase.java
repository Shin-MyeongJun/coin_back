package com.example.demo.analystics.application.port.in;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

import java.math.BigDecimal;

public interface PremiumAnalyticsUseCase {

    void onData(int partitionId, PremiumKey key, BigDecimal price);

    void flushCandles(Interval interval);

    void flushIndicators(Interval interval);
}
