package com.example.demo.market_data.application.usecase.base;

import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.market_data.application.port.in.PublishPriceDataUseCase;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class PublishPriceDataService<DOMAIN,MESSAGE> implements PublishPriceDataUseCase<DOMAIN> {

    private final DomainToMessage<DOMAIN,MESSAGE> mapper;
    private final PublishPriceValuePort<MESSAGE> port;

    @Override
    public void process(DOMAIN domain) {
        port.publish(mapper.toMessage(domain));
    }
}
