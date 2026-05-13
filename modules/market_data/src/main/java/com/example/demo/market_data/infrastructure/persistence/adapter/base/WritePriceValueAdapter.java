package com.example.demo.market_data.infrastructure.persistence.adapter.base;

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
        for (int start = 0; start < domains.size(); start += 1000) {
             List<DOMAIN> chunk = domains.subList(start, Math.min(start + 1000, domains.size()));
             var entities = chunk.stream().map(mapper::toEntity).toList();
             adapter.saveAll(entities);
        }
        adapter.flush();
    }
}
