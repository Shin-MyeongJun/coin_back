package com.example.demo.analystics.application.usecase.publish.candle;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.analystics.application.usecase.publish.PublishAnalyticsDataService;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PublishTickCandleService extends PublishAnalyticsDataService<TickCloseCandle, TickCandleMessage> {
    public PublishTickCandleService(PublishAnalyticValuePort<TickCandleMessage> publishPort, DomainToMessage<TickCloseCandle, TickCandleMessage> mapper) {
        super(publishPort, mapper);
    }
}
