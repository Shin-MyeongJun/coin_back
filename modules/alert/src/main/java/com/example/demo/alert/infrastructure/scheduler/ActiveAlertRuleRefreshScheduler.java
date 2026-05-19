package com.example.demo.alert.infrastructure.scheduler;

import com.example.demo.alert.application.port.out.ActiveAlertRuleStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveAlertRuleRefreshScheduler {

    private final ActiveAlertRuleStorePort activeAlertRuleStorePort;

    @Scheduled(fixedRateString = "${app.alert.evaluator.refresh-interval-ms:30000}")
    public void refresh() {
        activeAlertRuleStorePort.refresh();
    }
}
