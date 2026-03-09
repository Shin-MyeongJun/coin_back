package com.example.demo.market_data.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageMapping;
import com.example.demo.market_data.domain.domain.Tick;
import org.springframework.stereotype.Component;

@Component
public class TickMessageMapper implements
        MessageMapping<Tick, TickMessage>
 {
    @Override
    public TickMessage toMessage(Tick t) {
        return new TickMessage(
                t.marketCodeId(),
                t.bid(),
                t.ask(),
                t.timestamp()
        );
    }

    @Override
    public Tick toDomain(TickMessage tm) {
        return new Tick(
                tm.marketCodeId(),
                tm.bid(),
                tm.ask(),
                tm.timestamp()
        );
    }
}
