package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.mapper;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EcoIndCodeEntityMapperTest {

    private final EcoIndCodeEntityMapper sut = new EcoIndCodeEntityMapper();

    @Test
    @DisplayName("toEntity — domain의 5개 필드를 entity에 올바르게 매핑")
    void toEntity_mapsAllFields() {
        EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "CPI", ReleaseFrequency.MONTHLY, IndicatorUnit.PERCENT);

        EcoIndCodeEntity entity = sut.toEntity(code);

        assertThat(entity.getIndicatorCode()).isEqualTo(code.indicatorCode());
        assertThat(entity.getCountry()).isEqualTo("US");
        assertThat(entity.getType()).isEqualTo("CPI");
        assertThat(entity.getFrequency()).isEqualTo(ReleaseFrequency.MONTHLY);
        assertThat(entity.getUnit()).isEqualTo(IndicatorUnit.PERCENT);
    }

    @Test
    @DisplayName("toDomain — entity의 5개 필드를 domain에 올바르게 매핑")
    void toDomain_mapsAllFields() {
        EcoIndCodeEntity entity = EcoIndCodeEntity.builder()
                .indicatorCode("KR_GDP_QUARTERLY")
                .country("KR")
                .type("GDP")
                .frequency(ReleaseFrequency.QUARTERLY)
                .unit(IndicatorUnit.INDEX)
                .build();

        EconomicIndicatorCode code = sut.toDomain(entity);

        assertThat(code.indicatorCode()).isEqualTo("KR_GDP_QUARTERLY");
        assertThat(code.country()).isEqualTo("KR");
        assertThat(code.type()).isEqualTo("GDP");
        assertThat(code.frequency()).isEqualTo(ReleaseFrequency.QUARTERLY);
        assertThat(code.unit()).isEqualTo(IndicatorUnit.INDEX);
    }
}
