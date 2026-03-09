package com.example.demo.infra_heartbeat.infrastrcuture.cache;

import com.example.demo.infra_heartbeat.application.out.CachingHealthPort;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthValue;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Component
public class HealthCache implements CachingHealthPort {
    private final ConcurrentHashMap<HealthMeta, HealthValue> cache = new ConcurrentHashMap<>();
    @Override
    public HealthValue get(HealthMeta meta) {
        return cache.get(meta);
    }

    @Override
    public HealthValue compute(HealthMeta meta, BiFunction<HealthMeta, HealthValue, HealthValue> fn) {
        return cache.compute(meta, fn);
    }

    @Override
    public Map<HealthMeta, HealthValue> snapshot() {
        return Map.copyOf(cache);
    }









}
