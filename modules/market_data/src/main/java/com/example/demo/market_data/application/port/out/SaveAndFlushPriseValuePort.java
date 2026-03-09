package com.example.demo.market_data.application.port.out;

import java.util.List;

public interface SaveAndFlushPriseValuePort<ENTITY> {
    void saveAll(List<ENTITY> entity);
    void flush();
}
