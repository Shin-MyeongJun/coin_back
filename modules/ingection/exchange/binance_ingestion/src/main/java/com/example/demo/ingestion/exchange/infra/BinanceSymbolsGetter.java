package com.example.demo.ingestion.exchange.infra;


import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infre_exchange.client.impl.BinanceFutureConnector;
import com.example.demo.infre_exchange.dto.BinanceExchangeInfoDto;
import com.example.demo.ingestion.exchange.application.port.in.GetSymbolsUseCase;
import com.jsoniter.spi.TypeLiteral;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BinanceSymbolsGetter implements GetSymbolsUseCase {
    private final BinanceFutureConnector connector;

    @Override
    public List<String> getAll() {
       return connector.getMarketCodes()
                .map(json -> {
                    BinanceExchangeInfoDto dto = JsonUtil.fromJson(json, new TypeLiteral<BinanceExchangeInfoDto>() {});
                    return dto.getSymbols().stream()
                            .map(symbol -> symbol.getSymbol())
                            .toList();
                }).block();
    }
}
