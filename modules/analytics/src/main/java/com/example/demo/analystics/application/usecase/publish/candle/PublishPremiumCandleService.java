package com.example.demo.analystics.application.usecase.publish.candle;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumCandleService extends PublishAnalyticsDataService<PremiumCloseCandle, PremiumCandleMessage> {
    public PublishPremiumCandleService(PublishAnalyticValuePort<PremiumCandleMessage> publishPort, DomainToMessage<PremiumCloseCandle, PremiumCandleMessage> mapper) {
        super(publishPort, mapper);
    }
}
