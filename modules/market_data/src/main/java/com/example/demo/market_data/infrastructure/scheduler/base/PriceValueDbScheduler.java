package com.example.demo.market_data.infrastructure.scheduler.base;

import com.example.demo.market_data.application.port.in.FlushPriceValueBufferUseCase;
import com.example.demo.market_data.application.port.out.FlushPriceValueScheduled;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public abstract class PriceValueDbScheduler implements FlushPriceValueScheduled, SchedulingConfigurer {
    private final FlushPriceValueBufferUseCase useCase;
    protected long minDelayMs() { return 1_000; }      // 1초
    protected long maxDelayMs() { return 2_000; }

    @Override
    public void process() {
        useCase.flush();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.addTriggerTask(this::process, context -> {
            long delay = ThreadLocalRandom.current().nextLong(minDelayMs(), maxDelayMs() + 1); // [min,max]
            Instant base = (context.lastCompletion() != null)
                    ? context.lastCompletion()
                    : context.getClock().instant();
            return base.plusMillis(delay); // ✅ Instant를 직접 반환
        });
    }
}
