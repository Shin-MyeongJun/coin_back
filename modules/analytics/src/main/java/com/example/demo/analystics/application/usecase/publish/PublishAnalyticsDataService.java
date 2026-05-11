package com.example.demo.analystics.application.usecase.publish;

import com.example.demo.analystics.application.port.in.PublishAnalyticsDataUseCase;
import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract  class PublishAnalyticsDataService<DOMAIN,MESSAGE> implements PublishAnalyticsDataUseCase<DOMAIN> {
    private final PublishAnalyticValuePort<MESSAGE> publishPort;
    private final DomainToMessage<DOMAIN,MESSAGE> mapper;

    @Override
    public void publish(DOMAIN domain) {
       publishPort.publish(mapper.toMessage(domain));
    }
}
