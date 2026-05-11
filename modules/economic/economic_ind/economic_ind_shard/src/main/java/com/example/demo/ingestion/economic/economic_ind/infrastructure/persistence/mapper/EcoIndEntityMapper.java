package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoIndCodeCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcoIndEntityMapper implements DomainToEntity<EconomicRawIndicator,EcoIndEntity> {

    private final EcoIndCodeCache cache;

    @Override
    public EcoIndEntity toEntity(EconomicRawIndicator ei) {

        EconomicIndicatorCode code = ei.code();
        EconomicIndicatorValue value = ei.value();

        return EcoIndEntity.builder()
                .indCodeId(cache.getId(code))
                .value(value.value())
                .observationDate(toObservationDate(value.observationDate()))
                .releaseDate(value.releaseDate())
                .timestamp(value.timestamp())
                .build();
    }

    private Long toObservationDate(String observationDate) {
        if (observationDate == null || observationDate.isBlank()) {
            return null;
        }
        return Long.valueOf(observationDate.replace("-", ""));
    }
}
