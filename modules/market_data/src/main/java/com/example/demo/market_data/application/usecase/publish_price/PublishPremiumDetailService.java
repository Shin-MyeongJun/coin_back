package com.example.demo.market_data.application.usecase.publish_price;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import com.example.demo.market_data.application.usecase.base.PublishPriceDataService;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import org.springframework.stereotype.Component;

@Component
public class PublishPremiumDetailService extends PublishPriceDataService<PremiumDetail, PremiumDetailMessage> {


    public PublishPremiumDetailService(DomainToMessage<PremiumDetail, PremiumDetailMessage> mapper, PublishPriceValuePort<PremiumDetailMessage> port) {
        super(mapper, port);
    }
}
