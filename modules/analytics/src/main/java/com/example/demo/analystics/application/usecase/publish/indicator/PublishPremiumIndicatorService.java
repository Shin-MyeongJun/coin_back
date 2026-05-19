package com.example.demo.analystics.application.usecase.publish.indicator;

import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumIndicatorService extends PublishAnalyticsDataService<PremiumCloseIndicator, PremiumIndicatorMessage> {

    public PublishPremiumIndicatorService(
            DomainToMessage<PremiumCloseIndicator, PremiumIndicatorMessage> mapper,
            SaveOutboxRecordPort outboxSavePort,
            ObjectMapper objectMapper
    ) {
        super(mapper, outboxSavePort, objectMapper,
                "analytics.premium-indicator",
                "PREMIUM_INDICATOR",
                PremiumIndicatorMessage::extractKey);
    }
}
