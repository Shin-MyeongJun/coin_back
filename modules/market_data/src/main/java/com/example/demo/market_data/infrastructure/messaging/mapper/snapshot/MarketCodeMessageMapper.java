package com.example.demo.market_data.infrastructure.messaging.mapper.snapshot;

import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import org.springframework.stereotype.Component;

@Component
public class MarketCodeMessageMapper implements MessageToDomain<MarketCodeMessage, MarketCodeSnapShot> {
    @Override
    public MarketCodeSnapShot toDomain(MarketCodeMessage mcm) {
        return new MarketCodeSnapShot(
                mcm.id(),
                mcm.exchangeId(),
                mcm.base(),
                mcm.tradingPair()
        );
    }
}
