package com.example.demo.ingestion.exchange.infra.mapper;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infre_exchange.upbit.dto.UpbitMarketCodeDto;
import org.springframework.stereotype.Component;

@Component
public class UpbitMarketCodMapper implements DomainToMessage<UpbitMarketCodeDto, MarketCodeRawMessage> {
    private final String COUNTRY = "KR";
    private final String EXCHANGE_NAME = "UPBIT";
    private final String CONTRACT_TYPE = "SPOT";

    @Override
    public MarketCodeRawMessage toMessage(UpbitMarketCodeDto dto) {
        String[] parts = dto.getMarket().split("-");
        String base  = parts[1].toUpperCase();
        String quote = parts[0].toUpperCase();

        return new MarketCodeRawMessage(
                EXCHANGE_NAME,
                CONTRACT_TYPE,
                COUNTRY,
                quote,
                base,
                dto.getMarket()
        );
    }
}
