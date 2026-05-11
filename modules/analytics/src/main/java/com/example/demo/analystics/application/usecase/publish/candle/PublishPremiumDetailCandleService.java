package com.example.demo.analystics.application.usecase.publish.candle;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumDetailCandleService extends PublishAnalyticsDataService<PremiumDetailCloseCandle, PremiumDetailCandleMessage> {
    public PublishPremiumDetailCandleService(PublishAnalyticValuePort<PremiumDetailCandleMessage> publishPort, DomainToMessage<PremiumDetailCloseCandle, PremiumDetailCandleMessage> mapper) {
        super(publishPort, mapper);
    }
}
