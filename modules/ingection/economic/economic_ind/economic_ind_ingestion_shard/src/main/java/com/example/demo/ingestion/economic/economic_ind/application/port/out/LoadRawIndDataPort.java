package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import java.util.List;

public interface LoadRawIndDataPort<RAW> {
    List<RAW> getRaws();
    RAW getRaw(String target);
}
