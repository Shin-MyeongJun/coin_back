package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;

import java.util.List;

public interface ReadEcoIndCodePort {
    List<EcoIndCodeEntity> readAll();
}
