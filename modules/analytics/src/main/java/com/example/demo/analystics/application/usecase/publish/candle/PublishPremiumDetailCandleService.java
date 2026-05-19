package com.example.demo.analystics.application.usecase.publish.candle;

import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumDetailCandleService extends PublishAnalyticsDataService<PremiumDetailCloseCandle, PremiumDetailCandleMessage> {

    public PublishPremiumDetailCandleService(
            DomainToMessage<PremiumDetailCloseCandle, PremiumDetailCandleMessage> mapper,
            SaveOutboxRecordPort outboxSavePort,
            ObjectMapper objectMapper
    ) {
        super(mapper, outboxSavePort, objectMapper,
                "analytics.premium-detail-candle",
                "PREMIUM_DETAIL_CANDLE",
                PremiumDetailCandleMessage::extractKey);
    }
}
