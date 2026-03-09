package com.example.demo.analystics.infrastructure.persistence.adapter.base;

import com.datastax.oss.driver.shaded.guava.common.collect.Lists;
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
        var chunked = Lists.partition(domains, 1000);
        for (var chunk : chunked) {
            var entities = chunk.stream().map(mapper::toEntity).toList();
            adapter.saveAll(entities);
        }
        adapter.flush();
    }
}
