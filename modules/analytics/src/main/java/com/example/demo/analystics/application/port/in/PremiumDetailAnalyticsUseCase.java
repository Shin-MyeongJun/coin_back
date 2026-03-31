package com.example.demo.analystics.application.port.in;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;

public interface PremiumDetailAnalyticsUseCase {

    void onData(int partitionId, PremiumKey key, PremiumDetailValue value);

    void flushCandles(Interval interval);
}
