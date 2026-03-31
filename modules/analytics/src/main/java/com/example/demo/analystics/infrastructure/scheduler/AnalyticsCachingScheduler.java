package com.example.demo.analystics.infrastructure.scheduler;

import com.example.demo.analystics.application.usecase.caching.CachingPremiumDetailStateService;
import com.example.demo.analystics.application.usecase.caching.CachingPremiumStateService;
import com.example.demo.analystics.application.usecase.caching.CachingTickStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsCachingScheduler {

    private final CachingTickStateService tickCaching;
    private final CachingPremiumStateService premiumCaching;
    private final CachingPremiumDetailStateService premiumDetailCaching;

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void cacheAll() {
        try {
            tickCaching.caching();
            premiumCaching.caching();
            premiumDetailCaching.caching();
        } catch (Exception e) {
            log.error("[Caching] 상태 스냅샷 중 오류 발생", e);
        }
    }
}
