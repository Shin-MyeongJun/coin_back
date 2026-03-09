package com.example.demo.ingestion.exchange.infra;


import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infre_exchange.upbit.client.UpbitApiConnector;
import com.example.demo.infre_exchange.upbit.dto.UpbitMarketCodeDto;
import com.example.demo.ingestion.exchange.application.port.in.GetSymbolsUseCase;
import com.jsoniter.spi.TypeLiteral;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpbitSymbolGetter implements GetSymbolsUseCase {

    private final UpbitApiConnector connector;

    @Override
    public List<String> getAll() {
        return connector.getMarketsAll().map(
                json ->{
                    List<UpbitMarketCodeDto> list = JsonUtil.fromJson(json, new TypeLiteral<List<UpbitMarketCodeDto>>() {});
                    return list
                            .stream()
                            .map(UpbitMarketCodeDto::getMarket)
                            .toList();
                }).block();
    }
}
