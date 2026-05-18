package com.example.demo.alert.infrastructure.messaging.consumer;

import com.example.demo.alert.application.port.in.EvaluateMarketSignalUseCase;
import com.example.demo.alert.infrastructure.messaging.mapper.TickMessageToSignal;
import com.example.demo.contracts.message.price_value.TickMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickAlertConsumer {
    private final TickMessageToSignal mapper;
    private final EvaluateMarketSignalUseCase useCase;

    @KafkaListener(
            topics = "market-data.tick",
            groupId = "alert.tick",
            containerFactory = "tickAlertKafkaListenerContainerFactory"
    )
    public void onMessage(TickMessage message) {
        useCase.onSignal(mapper.toDomain(message));
    }
}
