package com.example.demo.market_data.infrastructure.persistence.adapter.base;

import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.market_data.application.port.out.WritePriceValuePort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class WritePriceValueAdapter<DOMAIN,ENTITY> implements WritePriceValuePort<DOMAIN> {
    private final PriceValueSaveAdapter<ENTITY> adapter;
    private final DomainToEntity<DOMAIN,ENTITY> mapper;

    @Override
    public void saveAll(List<DOMAIN> domains) {
        var chunked = Lists.partition(domains, 1000);
        for (var chunk : chunked) {
             var entities = chunk.stream().map(mapper::toEntity).toList();
             adapter.saveAll(entities);
        }
        adapter.flush();
    }
}
