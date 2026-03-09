package com.example.demo.market_data.infrastructure.messaging.mapper.snapshot;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import org.springframework.stereotype.Component;

@Component
public class ExchangeMessageMapper implements MessageToDomain<ExchangeMessage, ExchangeSnapShot> {
    @Override
    public ExchangeSnapShot toDomain(ExchangeMessage em) {
        return new ExchangeSnapShot(
                em.id(),
                em.name(),
                em.type(),
                em.quote(),
                "ALIVE"
        );
    }
}
