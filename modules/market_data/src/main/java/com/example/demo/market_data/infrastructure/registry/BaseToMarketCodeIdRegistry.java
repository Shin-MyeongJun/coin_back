package com.example.demo.market_data.infrastructure.registry;

import com.example.demo.market_data.application.port.out.PremiumMarketCodeRegistryPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BaseToMarketCodeIdRegistry implements PremiumMarketCodeRegistryPort {
    private final Map<String, Set<Long>> marketCodesByBase = new ConcurrentHashMap<>();

    public void put(String baseAsset, Long marketCodeId) {

        Set<Long> codes = marketCodesByBase.computeIfAbsent(baseAsset.toUpperCase(), key -> ConcurrentHashMap.newKeySet());
        codes.add(marketCodeId);
    }

    public Set<Long> get(String baseAsset) {
        Set<Long> codes = marketCodesByBase.get(baseAsset.toUpperCase());
        if (codes == null) {
            return Set.of();
        }
        return Set.copyOf(codes);
    }

    public Set<Long> getAll() {
        Set<Long> result = ConcurrentHashMap.newKeySet();
        for (Set<Long> codes : marketCodesByBase.values()) {
            result.addAll(codes);
        }
        return Set.copyOf(result);
    }

    public void remove(Long marketCodeId) {
        for (Set<Long> codes : marketCodesByBase.values()) {
            codes.remove(marketCodeId);
        }
    }
}
