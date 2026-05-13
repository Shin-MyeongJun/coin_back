package com.example.demo.analystics.infrastructure.persistence.adapter.base;

import com.example.demo.analystics.application.port.out.FlushAndSaveAnalyticValuePort;

import com.example.demo.analystics.application.port.out.WriteAnalyticsValuePort;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class WriteAnalyticsAdapter<DOMAIN,ENTITY> implements WriteAnalyticsValuePort<DOMAIN> {

    private final FlushAndSaveAnalyticValuePort<ENTITY> adapter;
    private final DomainToEntity<DOMAIN,ENTITY> mapper;

    @Override
    public void write(List<DOMAIN> domains) {
        for (int start = 0; start < domains.size(); start += 1000) {
            List<DOMAIN> chunk = domains.subList(start, Math.min(start + 1000, domains.size()));
            var entities = chunk.stream().map(mapper::toEntity).toList();
            adapter.saveAll(entities);
        }
        adapter.flush();
    }
}
