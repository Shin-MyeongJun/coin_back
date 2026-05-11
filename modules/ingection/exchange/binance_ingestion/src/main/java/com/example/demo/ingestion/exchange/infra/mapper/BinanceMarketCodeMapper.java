package com.example.demo.ingestion.exchange.infra.mapper;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infre_exchange.dto.BinanceExchangeInfoDto;
import org.springframework.stereotype.Component;

@Component
public class BinanceMarketCodeMapper implements DomainToMessage<BinanceExchangeInfoDto.Symbol , MarketCodeRawMessage> {
    String EXCHANGE_NAME = "BINANCE";
    String COUNTRY = "US";

    @Override
    public MarketCodeRawMessage toMessage(BinanceExchangeInfoDto.Symbol symbol) {
        return new MarketCodeRawMessage(
                EXCHANGE_NAME,
                symbol.getContractType(),
                COUNTRY,
                symbol.getQuoteAsset().toUpperCase(),
                symbol.getBaseAsset().toUpperCase(),
                symbol.getSymbol().toUpperCase()
        );
    }
}
