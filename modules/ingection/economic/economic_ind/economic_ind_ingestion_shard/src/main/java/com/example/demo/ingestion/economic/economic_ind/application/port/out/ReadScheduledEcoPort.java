package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;

import java.util.List;

public interface ReadScheduledEcoPort {
    List<EconomicScheduleEntity> readPending();
}
