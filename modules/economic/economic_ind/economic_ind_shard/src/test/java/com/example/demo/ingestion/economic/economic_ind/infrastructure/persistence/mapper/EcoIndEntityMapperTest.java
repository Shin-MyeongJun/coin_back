package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.mapper;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoIndCodeCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EcoIndEntityMapperTest {

    @Mock
    EcoIndCodeCache cache;

    @InjectMocks
    EcoIndEntityMapper sut;

    @Test
    @DisplayName("toEntity — cache로 indCodeId를 조회하고 value 필드를 모두 매핑")
    void toEntity_usesCache_andMapsAllValueFields() {
        EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "CPI", ReleaseFrequency.MONTHLY, IndicatorUnit.PERCENT);
        // observationDate must be a numeric string (Long.valueOf is called internally)
        EconomicIndicatorValue value = new EconomicIndicatorValue(
                new BigDecimal("3.5"), "1700000000", 1_699_000_000L, 1_700_000_001L);
        EconomicRawIndicator domain = new EconomicRawIndicator(code, value);

        given(cache.getId(code)).willReturn(42L);

        EcoIndEntity entity = sut.toEntity(domain);

        assertThat(entity.getIndCodeId()).isEqualTo(42L);
        assertThat(entity.getValue()).isEqualByComparingTo(new BigDecimal("3.5"));
        assertThat(entity.getObservationDate()).isEqualTo(1_700_000_000L);
        assertThat(entity.getReleaseDate()).isEqualTo(1_699_000_000L);
        assertThat(entity.getTimestamp()).isEqualTo(1_700_000_001L);
    }
}
