package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import java.util.List;

public interface ReadScheduledEcoPort {
    List<EconomicSchedule> readPending();
}
