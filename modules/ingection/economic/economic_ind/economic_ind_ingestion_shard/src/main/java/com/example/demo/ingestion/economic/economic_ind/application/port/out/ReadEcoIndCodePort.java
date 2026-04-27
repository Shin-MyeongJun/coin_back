package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import java.util.List;

public interface ReadEcoIndCodePort {
    List<EconomicIndicatorCode> readAll();
}
