package com.example.demo.meta_data.infrastructure.persistence.mapper;

import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
import org.springframework.stereotype.Component;

@Component
public class MarketCodeMapper implements MessageToDomain<MarketCodeRawMessage, MarketCodeEntity>,
        DomainToMessage<MarketCodeEntity, MarketCodeMessage> {

    @Override
    public MarketCodeEntity toDomain(MarketCodeRawMessage raw) {
        MarketCodeKey key =MarketCodeKey.builder()
                .exchangeId(1L)
                .quoteAsset(raw.asset())
                .baseAsset(raw.base())
                .tradingPair(raw.tradingPair())
                .build();

        return MarketCodeEntity.builder()
                .key(key)
                .build();
    }

    @Override
    public MarketCodeMessage toMessage(MarketCodeEntity me) {
        return new MarketCodeMessage(
                me.getId(),
                me.getKey().getExchangeId(),
                me.getKey().getBaseAsset(),
                me.getKey().getTradingPair()
        );
    }
}
