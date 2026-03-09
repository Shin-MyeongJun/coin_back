package com.example.demo.market_data.application.port.out;

import java.util.List;

public interface WritePriceValuePort<DOMAIN> {
    public void saveAll(List<DOMAIN > domains);
}
