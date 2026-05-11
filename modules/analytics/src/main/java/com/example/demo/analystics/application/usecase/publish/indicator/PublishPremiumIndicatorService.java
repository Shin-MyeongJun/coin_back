package com.example.demo.analystics.application.usecase.publish.indicator;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumIndicatorService extends PublishAnalyticsDataService<PremiumCloseIndicator, PremiumIndicatorMessage> {
    public PublishPremiumIndicatorService(PublishAnalyticValuePort<PremiumIndicatorMessage> publishPort, DomainToMessage<PremiumCloseIndicator, PremiumIndicatorMessage> mapper) {
        super(publishPort, mapper);
    }
}
