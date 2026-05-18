package com.example.demo.alert.infrastructure.messaging.consumer;

import com.example.demo.alert.application.port.in.EvaluateMarketSignalUseCase;
import com.example.demo.alert.infrastructure.messaging.mapper.PremiumDetailMessageToSignal;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailAlertConsumer {
    private final PremiumDetailMessageToSignal mapper;
    private final EvaluateMarketSignalUseCase useCase;

    @KafkaListener(
            topics = "market-data.premium-detail",
            groupId = "alert.premium-detail",
            containerFactory = "premiumDetailAlertKafkaListenerContainerFactory"
    )
    public void onMessage(PremiumDetailMessage message) {
        useCase.onSignal(mapper.toDomain(message));
    }
}
