package com.example.demo.ingestion.economic.economic_ind.infrastructure.parer;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.FredObservationResultDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FredIndMapper implements RawToDomain<FredObservationResultDto, EconomicRawIndicator> {

    @Override
    public EconomicRawIndicator toDomain(FredObservationResultDto raw, Map<String, String> args) {

        EconomicIndicatorCode code = EconomicIndicatorCode.of(
                args.get("country"),
                args.get("type"),
                raw.frequency(),
                raw.units()
        );
        EconomicIndicatorValue value = new EconomicIndicatorValue(
                raw.value(),
                raw.
        )

        return new EconomicRawIndicator(
                code,
                value
        );
    }
}
