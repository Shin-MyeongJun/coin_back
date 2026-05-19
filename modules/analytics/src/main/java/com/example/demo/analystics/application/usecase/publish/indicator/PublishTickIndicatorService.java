package com.example.demo.analystics.application.usecase.publish.indicator;

import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PublishTickIndicatorService extends PublishAnalyticsDataService<TickCloseIndicator, TickIndicatorMessage> {

    public PublishTickIndicatorService(
            DomainToMessage<TickCloseIndicator, TickIndicatorMessage> mapper,
            SaveOutboxRecordPort outboxSavePort,
            ObjectMapper objectMapper
    ) {
        super(mapper, outboxSavePort, objectMapper,
                "analytics.tick-indicator",
                "TICK_INDICATOR",
                TickIndicatorMessage::extractKey);
    }
}
