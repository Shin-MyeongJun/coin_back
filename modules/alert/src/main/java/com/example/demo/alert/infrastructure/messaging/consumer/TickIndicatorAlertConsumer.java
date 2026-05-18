package com.example.demo.alert.infrastructure.messaging.consumer;

import com.example.demo.alert.application.port.in.EvaluateMarketSignalUseCase;
import com.example.demo.alert.infrastructure.messaging.mapper.TickIndicatorMessageToSignal;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickIndicatorAlertConsumer {
    private final TickIndicatorMessageToSignal mapper;
    private final EvaluateMarketSignalUseCase useCase;

    @KafkaListener(
            topics = "analytics.tick-indicator",
            groupId = "alert.tick-indicator",
            containerFactory = "tickIndicatorAlertKafkaListenerContainerFactory"
    )
    public void onMessage(TickIndicatorMessage message) {
        useCase.onSignal(mapper.toDomain(message));
    }
}
