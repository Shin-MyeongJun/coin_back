package com.example.demo.market_data.application.usecase.publish_price;

import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import com.example.demo.market_data.application.usecase.base.PublishPriceDataService;
import com.example.demo.market_data.domain.domain.Tick;
import org.springframework.stereotype.Component;

@Component
public class PublishTickService extends PublishPriceDataService<Tick, TickMessage> {
    public PublishTickService(DomainToMessage<Tick, TickMessage> mapper, PublishPriceValuePort<TickMessage> port) {
        super(mapper, port);
    }
}
