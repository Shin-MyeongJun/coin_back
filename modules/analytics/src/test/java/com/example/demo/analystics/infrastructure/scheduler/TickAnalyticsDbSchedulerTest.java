package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.port.in.TickAnalyticsUseCase;
import com.example.demo.analystics.domain.domain.Interval;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TickAnalyticsDbSchedulerTest {

    @Mock
    TickAnalyticsUseCase useCase;

    @InjectMocks
    TickAnalyticsDbScheduler sut;

    @Test
    @DisplayName("process(M1) — flushCandles(M1) + flushIndicators(M1) 각 1회")
    void process_m1_flushesCandles_and_Indicators() {
        // when
        sut.process(Interval.M1);

        // then
        then(useCase).should().flushCandles(Interval.M1);
        then(useCase).should().flushIndicators(Interval.M1);
    }

    @Test
    @DisplayName("process(M5) — flushCandles(M5) + flushIndicators(M5) 각 1회")
    void process_m5_flushesCorrectInterval() {
        // when
        sut.process(Interval.M5);

        // then
        then(useCase).should().flushCandles(Interval.M5);
        then(useCase).should().flushIndicators(Interval.M5);
    }

    @Test
    @DisplayName("m1() 트리거 — process(M1) 호출")
    void m1_trigger_callsProcessM1() {
        // when
        sut.m1();

        // then
        then(useCase).should().flushCandles(Interval.M1);
        then(useCase).should().flushIndicators(Interval.M1);
    }
}
