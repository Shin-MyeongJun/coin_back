package com.example.demo.alert.infrastructure.messaging.consumer;

import com.example.demo.alert.application.port.in.EvaluateMarketSignalUseCase;
import com.example.demo.alert.infrastructure.messaging.mapper.PremiumMessageToSignal;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumAlertConsumer {
    private final PremiumMessageToSignal mapper;
    private final EvaluateMarketSignalUseCase useCase;

    @KafkaListener(
            topics = "market-data.premium",
            groupId = "alert.premium",
            containerFactory = "premiumAlertKafkaListenerContainerFactory"
    )
    public void onMessage(PremiumMessage message) {
        useCase.onSignal(mapper.toDomain(message));
    }
}
