package com.example.demo.ingestion.exchange.infra;


import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infre_exchange.client.impl.BinanceFutureConnector;
import com.example.demo.infre_exchange.dto.BinanceExchangeInfoDto;
import com.example.demo.ingestion.exchange.application.port.in.GetMarketCodeRaw;
import com.jsoniter.spi.TypeLiteral;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class BinanceMarketCodeGetter implements GetMarketCodeRaw {
    private final String COUNTRY = "US";
    private final String EXCHANGE_NAME = "BINANCE";

    private final BinanceFutureConnector connector;
    private final DomainToMessage<BinanceExchangeInfoDto.Symbol , MarketCodeRawMessage> mapper;


    @Override
    public List<MarketCodeRawMessage> getAll() {
       return   connector.getMarketCodes().map(json ->{
            BinanceExchangeInfoDto dto = JsonUtil.fromJson(json, new TypeLiteral<BinanceExchangeInfoDto>() {});
            return dto.getSymbols()
                    .stream()
                    .map(mapper::toMessage)
                    .toList();

         }).block();
    }
}
