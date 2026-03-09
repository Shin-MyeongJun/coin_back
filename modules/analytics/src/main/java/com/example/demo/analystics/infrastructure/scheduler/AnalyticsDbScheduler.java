package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.port.in.IntervalFlushUseCase;
import com.example.demo.analystics.application.port.out.SchedulingAnalyticsValuePort;
import com.example.demo.analystics.domain.domain.Interval;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;


@RequiredArgsConstructor
public abstract class AnalyticsDbScheduler implements SchedulingAnalyticsValuePort {

    private final IntervalFlushUseCase useCase;

    @Override
    public void process(Interval interval) {
            useCase.flush(interval);
    }

    // 매 1분 정각
    @Scheduled(cron = "0 * * * * *")
    public void m1() { process(Interval.M1); }

    // 매 3분 정각
    @Scheduled(cron = "0 */3 * * * *")
    public void m3() { process(Interval.M3); }

    // 매 5분 정각
    @Scheduled(cron = "0 */5 * * * *")
    public void m5() { process(Interval.M5); }

    // 매 15분 정각
    @Scheduled(cron = "0 */15 * * * *")
    public void m15() { process(Interval.M15); }

    // 매 30분 정각
    @Scheduled(cron = "0 */30 * * * *")
    public void m30() { process(Interval.M30); }

    // 매 1시간 정각
    @Scheduled(cron = "0 0 * * * *")
    public void h1() { process(Interval.M60); }

    // 매 4시간 정각
    @Scheduled(cron = "0 0 */4 * * *")
    public void h4() { process(Interval.M240); }
}


