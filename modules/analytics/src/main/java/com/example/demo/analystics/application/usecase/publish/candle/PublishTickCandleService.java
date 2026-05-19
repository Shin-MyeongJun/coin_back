package com.example.demo.analystics.application.usecase.publish.candle;

import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PublishTickCandleService extends PublishAnalyticsDataService<TickCloseCandle, TickCandleMessage> {

    public PublishTickCandleService(
            DomainToMessage<TickCloseCandle, TickCandleMessage> mapper,
            SaveOutboxRecordPort outboxSavePort,
            ObjectMapper objectMapper
    ) {
        super(mapper, outboxSavePort, objectMapper,
                "analytics.tick-candle",
                "TICK_CANDLE",
                TickCandleMessage::extractKey);
    }
}
