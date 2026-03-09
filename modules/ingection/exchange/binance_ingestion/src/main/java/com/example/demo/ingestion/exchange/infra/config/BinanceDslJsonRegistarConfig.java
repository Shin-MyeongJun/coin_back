package com.example.demo.ingestion.exchange.infra.config;


import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserRegistrar;
import com.example.demo.infre_exchange.dto.stream.BinanceBookTickerDto;
import com.example.demo.infre_exchange.dto.stream.BinanceStreamFormat;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BinanceDslJsonRegistarConfig implements DslJsonParserRegistrar {
    @Override
    public void register(DslJsonParserManager r) {
       r.registerGeneric("binanceTick", BinanceStreamFormat.class, BinanceBookTickerDto.class);
    }
}
