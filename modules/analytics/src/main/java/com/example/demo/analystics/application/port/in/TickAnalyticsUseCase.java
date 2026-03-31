package com.example.demo.analystics.application.port.in;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.key.TickKey;

public interface TickAnalyticsUseCase {

    /** Kafka Consumer가 호출. 실시간 틱 데이터를 수신한다. */
    void onData(int partitionId, TickKey key, java.math.BigDecimal price);

    /** 스케줄러가 호출. 해당 Interval의 캔들을 flush하여 저장·발행한다. */
    void flushCandles(Interval interval);

    /** 스케줄러가 호출. 해당 Interval의 지표를 flush하여 저장·발행한다. */
    void flushIndicators(Interval interval);
}
