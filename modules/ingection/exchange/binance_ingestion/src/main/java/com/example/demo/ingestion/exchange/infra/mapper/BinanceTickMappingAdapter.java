package com.example.demo.ingestion.exchange.infra.mapper;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infre_exchange.dto.stream.BinanceBookTickerDto;
import com.example.demo.ingestion.exchange.application.port.out.GetCachedDataPort;
import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinanceTickMappingAdapter implements DomainToMessage<BinanceBookTickerDto, TickRawMessage> {
    private final static String EXCHANGE_NAME = "BINANCE";
    private final GetCachedDataPort.forMarketCode getter;

    @Override
    public TickRawMessage toMessage(BinanceBookTickerDto dto) {
        String exchangeType = "UNKNOWN";
        String quote = "UNKNOWN";
        String base = "UNKNOWN";
        MarketCodeKey key= new MarketCodeKey(
                EXCHANGE_NAME,
                dto.s()
        );
        MarketCodeValue val = getter.get(key);
        if(val != null){
            quote = val.quote().toUpperCase();
            base = val.base().toUpperCase();
            exchangeType = val.exchangeType();
        }
        return new TickRawMessage(
                dto.s(),
                EXCHANGE_NAME,
                exchangeType,
                quote,
                base,
                dto.b(),
                dto.a(),
                System.currentTimeMillis()
        );
    }


}
