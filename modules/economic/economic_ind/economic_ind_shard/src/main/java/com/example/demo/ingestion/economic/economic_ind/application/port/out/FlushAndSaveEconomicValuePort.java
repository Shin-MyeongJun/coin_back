package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import java.util.List;

public interface FlushAndSaveEconomicValuePort<DOMAIN> {
    void flush();
    void saveAll(List<DOMAIN> domains);
}
