package com.example.demo.market_data.application.usecase.publish_price;

import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import com.example.demo.market_data.application.usecase.base.PublishPriceDataService;
import com.example.demo.market_data.domain.domain.Premium;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumService extends PublishPriceDataService<Premium, PremiumMessage> {
    public PublishPremiumService(DomainToMessage<Premium, PremiumMessage> mapper, PublishPriceValuePort<PremiumMessage> port) {
        super(mapper, port);
    }
}
