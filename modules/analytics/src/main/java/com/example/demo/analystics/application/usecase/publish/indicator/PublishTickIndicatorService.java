package com.example.demo.analystics.application.usecase.publish.indicator;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PublishTickIndicatorService extends PublishAnalyticsDataService<TickCloseIndicator, TickIndicatorMessage> {
    public PublishTickIndicatorService(PublishAnalyticValuePort<TickIndicatorMessage> publishPort, DomainToMessage<TickCloseIndicator, TickIndicatorMessage> mapper) {
        super(publishPort, mapper);
    }
}
