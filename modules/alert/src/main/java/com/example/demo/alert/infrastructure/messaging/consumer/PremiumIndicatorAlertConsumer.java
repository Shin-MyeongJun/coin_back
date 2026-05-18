package com.example.demo.alert.infrastructure.messaging.consumer;

import com.example.demo.alert.application.port.in.EvaluateMarketSignalUseCase;
import com.example.demo.alert.infrastructure.messaging.mapper.PremiumIndicatorMessageToSignal;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumIndicatorAlertConsumer {
    private final PremiumIndicatorMessageToSignal mapper;
    private final EvaluateMarketSignalUseCase useCase;

    @KafkaListener(
            topics = "analytics.premium-indicator",
            groupId = "alert.premium-indicator",
            containerFactory = "premiumIndicatorAlertKafkaListenerContainerFactory"
    )
    public void onMessage(PremiumIndicatorMessage message) {
        useCase.onSignal(mapper.toDomain(message));
    }
}
