package com.example.demo.api.alert;

import com.example.demo.alert.application.port.in.EvaluatePremiumAlertUseCase;
import com.example.demo.api.stream.sink.MarketDataStream;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumAlertBridge {

    private final MarketDataStream marketDataStream;
    private final ObjectProvider<EvaluatePremiumAlertUseCase> evaluatePremiumAlertUseCaseProvider;

    @Value("${app.alert.evaluator.enabled:false}")
    private boolean enabled;

    private volatile Disposable subscription;

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("alert evaluator disabled — PremiumAlertBridge will not subscribe to premiumSink");
            return;
        }
        EvaluatePremiumAlertUseCase useCase = evaluatePremiumAlertUseCaseProvider.getIfAvailable();
        if (useCase == null) {
            log.warn("alert evaluator enabled but EvaluatePremiumAlertUseCase bean not available — bridge inactive");
            return;
        }
        subscription = marketDataStream.premiumSink.asFlux().subscribe(
                message -> dispatch(useCase, message),
                error -> log.error("PremiumAlertBridge subscription error: {}", error.getMessage(), error)
        );
        log.info("PremiumAlertBridge subscribed to premiumSink");
    }

    private void dispatch(EvaluatePremiumAlertUseCase useCase, PremiumMessage message) {
        try {
            useCase.evaluate(message);
        } catch (IllegalArgumentException e) {
            log.warn("PremiumAlertBridge skipped message due to unsupported rule operator: {}", e.getMessage());
        } catch (Exception e) {
            log.error("PremiumAlertBridge dispatch failed: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stop() {
        Disposable current = subscription;
        if (current != null && !current.isDisposed()) {
            current.dispose();
            log.info("PremiumAlertBridge subscription disposed");
        }
    }
}
