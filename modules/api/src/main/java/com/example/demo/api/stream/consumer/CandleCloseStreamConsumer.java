package com.example.demo.api.stream.consumer;

import com.example.demo.api.stream.sink.AnalyticsStream;
import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CandleCloseStreamConsumer {

    private final AnalyticsStream analyticsStream;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "analytics.tick-candle",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            containerFactory = "streamKafkaListenerContainerFactory"
    )
    public void onTickCandle(ConsumerRecord<String, String> record) {
        try {
            TickCandleMessage message = objectMapper.readValue(record.value(), TickCandleMessage.class);
            analyticsStream.tickCandleSink.tryEmitNext(message);
        } catch (Exception e) {
            log.warn("Failed to deserialize TickCandleMessage: {}", e.getMessage());
        }
    }

    @KafkaListener(
            topics = "analytics.premium-candle",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            containerFactory = "streamKafkaListenerContainerFactory"
    )
    public void onPremiumCandle(ConsumerRecord<String, String> record) {
        try {
            PremiumCandleMessage message = objectMapper.readValue(record.value(), PremiumCandleMessage.class);
            analyticsStream.premiumCandleSink.tryEmitNext(message);
        } catch (Exception e) {
            log.warn("Failed to deserialize PremiumCandleMessage: {}", e.getMessage());
        }
    }

    @KafkaListener(
            topics = "analytics.premium-detail-candle",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            containerFactory = "streamKafkaListenerContainerFactory"
    )
    public void onPremiumDetailCandle(ConsumerRecord<String, String> record) {
        try {
            PremiumDetailCandleMessage message = objectMapper.readValue(record.value(), PremiumDetailCandleMessage.class);
            analyticsStream.premiumDetailCandleSink.tryEmitNext(message);
        } catch (Exception e) {
            log.warn("Failed to deserialize PremiumDetailCandleMessage: {}", e.getMessage());
        }
    }
}
