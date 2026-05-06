package com.example.demo.api.stream.consumer;

import com.example.demo.api.stream.sink.AnalyticsStream;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndicatorCloseStreamConsumer {

    private final AnalyticsStream analyticsStream;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "analytics.tick-indicator",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            containerFactory = "streamKafkaListenerContainerFactory"
    )
    public void onTickIndicator(ConsumerRecord<String, String> record) {
        try {
            TickIndicatorMessage message = objectMapper.readValue(record.value(), TickIndicatorMessage.class);
            analyticsStream.tickIndicatorSink.tryEmitNext(message);
        } catch (Exception e) {
            log.warn("Failed to deserialize TickIndicatorMessage: {}", e.getMessage());
        }
    }

    @KafkaListener(
            topics = "analytics.premium-indicator",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            containerFactory = "streamKafkaListenerContainerFactory"
    )
    public void onPremiumIndicator(ConsumerRecord<String, String> record) {
        try {
            PremiumIndicatorMessage message = objectMapper.readValue(record.value(), PremiumIndicatorMessage.class);
            analyticsStream.premiumIndicatorSink.tryEmitNext(message);
        } catch (Exception e) {
            log.warn("Failed to deserialize PremiumIndicatorMessage: {}", e.getMessage());
        }
    }
}
