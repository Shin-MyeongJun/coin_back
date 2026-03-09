package com.example.demo.ingestion.exchange.infra.config;


import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserRegistrar;
import com.example.demo.infre_exchange.upbit.dto.UpbitMyAssetDto;
import com.example.demo.infre_exchange.upbit.dto.UpbitMyOrderDto;
import com.example.demo.infre_exchange.upbit.dto.UpbitOrderbookDto;
import com.example.demo.infre_exchange.upbit.dto.UpbitTickerDto;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpbitDslJsonParserConfig implements DslJsonParserRegistrar {
    @Override
    public void register(DslJsonParserManager r) {
        r.register("upbitMyAsset", UpbitMyAssetDto.class);
        r.register("upbitMyOrder", UpbitMyOrderDto.class);
        r.register("upbitOrderbook", UpbitOrderbookDto.class);
        r.register("upbitTick", UpbitTickerDto.class);
    }
}
