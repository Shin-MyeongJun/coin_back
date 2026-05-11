package com.example.demo.market_data.infrastructure.messaging.mapper.raw;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class TickRawMessageMappingAdapter implements MessageToDomain<TickRawMessage, Tick> {

    private  final GetCacheDataPort<ExchangeSnapShotVal,Long> exchangeIdCache;
    private final  GetCacheDataPort<MarketCodeSnapShotVal,Long> marketCodeIdCache;


    @Override
    public Tick toDomain(TickRawMessage raw) {
        Optional<Long> exchangeId = exchangeIdCache.get(new ExchangeSnapShotVal(
                raw.exchange(),
                raw.exchangeType(),
                raw.quote()
        ));
        if (exchangeId.isEmpty())return null;

        Optional<Long> marketCodeId = marketCodeIdCache.get(
                new MarketCodeSnapShotVal(
                        exchangeId.get(),
                        raw.base(),
                        raw.tradingPair()
                )
        );

        return marketCodeId.map(aLong -> new Tick(
                aLong,
                new BigDecimal(raw.bid()),
                new BigDecimal(raw.ask()),
                raw.timestamp()
        )).orElse(null);

    }
}
