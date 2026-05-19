package com.example.demo.analystics.application.usecase.publish.candle;

import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumCandleService extends PublishAnalyticsDataService<PremiumCloseCandle, PremiumCandleMessage> {

    public PublishPremiumCandleService(
            DomainToMessage<PremiumCloseCandle, PremiumCandleMessage> mapper,
            SaveOutboxRecordPort outboxSavePort,
            ObjectMapper objectMapper
    ) {
        super(mapper, outboxSavePort, objectMapper,
                "analytics.premium-candle",
                "PREMIUM_CANDLE",
                PremiumCandleMessage::extractKey);
    }
}
