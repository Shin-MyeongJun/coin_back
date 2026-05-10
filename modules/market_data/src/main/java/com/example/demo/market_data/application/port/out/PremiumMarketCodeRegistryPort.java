package com.example.demo.market_data.application.port.out;

import java.util.Set;

public interface PremiumMarketCodeRegistryPort{
    void put(String baseAsset, Long marketCodeId);
    Set<Long> get(String baseAsset);
    Set<Long> getAll();
    void remove(Long marketCodeId);
}
