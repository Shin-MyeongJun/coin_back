package com.example.demo.market_data.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageMapping;
import com.example.demo.market_data.domain.domain.Premium;
import org.springframework.stereotype.Component;

@Component
public class PremiumMessageMapper implements
        MessageMapping<Premium, PremiumMessage>
 {


    @Override
    public Premium toDomain(PremiumMessage pm) {
        return new Premium(
                pm.symbol(),
                pm.baseExchangeId(),
                pm.compareExchangeId(),
                pm.bid(),
                pm.ask(),
                pm.timestamp()
        );
    }

    @Override
    public PremiumMessage toMessage(Premium p) {
        return new PremiumMessage(
                p.symbol(),
                p.baseExchangeId(),
                p.compareExchangeId(),
                p.bid(),
                p.ask(),
                p.timestamp()
        );
    }
}
