package com.example.demo.meta_data.infrastructure.persistence.mapper;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.meta_data.domain.ExchangeStatus;
import com.example.demo.meta_data.domain.ExchangeType;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import org.springframework.stereotype.Component;

@Component
public class ExchangeMapper implements MessageToDomain<MarketCodeRawMessage, ExchangeEntity> ,
        DomainToMessage<ExchangeEntity, ExchangeMessage> {
    @Override
    public ExchangeEntity toDomain(MarketCodeRawMessage raw) {
        ExchangeKey key= ExchangeKey.builder()
                .name(raw.exchangeName())
                .exchangeType(ExchangeType.fromValue(raw.exchangeType()))
                .quote(raw.asset())
                .country(raw.country())
                .build();

        return ExchangeEntity.builder()
                .key(key)
                .status(ExchangeStatus.ACTIVE)
                .build();
    }

    @Override
    public ExchangeMessage toMessage(ExchangeEntity en) {
        return new ExchangeMessage(
                en.getId(),
                en.getKey().getName(),
                en.getKey().getExchangeType().toString(),
                en.getKey().getQuote()
        );
    }
}
