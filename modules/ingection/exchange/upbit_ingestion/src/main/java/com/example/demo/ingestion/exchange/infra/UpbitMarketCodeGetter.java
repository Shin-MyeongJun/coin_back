package com.example.demo.ingestion.exchange.infra;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infre_exchange.upbit.client.UpbitApiConnector;
import com.example.demo.infre_exchange.upbit.dto.UpbitMarketCodeDto;
import com.example.demo.ingestion.exchange.application.port.in.GetMarketCodeRaw;
import com.jsoniter.spi.TypeLiteral;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpbitMarketCodeGetter implements GetMarketCodeRaw {

    private final UpbitApiConnector connector;
    private final DomainToMessage<UpbitMarketCodeDto, MarketCodeRawMessage> mapper;


    @Override
    public List<MarketCodeRawMessage> getAll() {
       return connector.getMarketsAll().map(json ->{
            List<UpbitMarketCodeDto> list = JsonUtil.fromJson(json, new TypeLiteral<List<UpbitMarketCodeDto>>() {});
            return list
                    .stream()
                    .map(mapper::toMessage)
                    .toList();

        }).block();
    }
}
