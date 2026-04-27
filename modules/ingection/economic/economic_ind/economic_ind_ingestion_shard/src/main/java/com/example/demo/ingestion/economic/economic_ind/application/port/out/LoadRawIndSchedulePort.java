package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import java.util.List;

public interface LoadRawIndSchedulePort<RAW> {
    List<RAW> getRawSchedules();
}
