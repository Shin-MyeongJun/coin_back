package com.example.demo.ingestion.economic.economic_ind.domain;

import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EconomicIndicatorCodeTest {

    @Test
    @DisplayName("of — indicatorCode를 'country_type_frequency' 형식으로 생성")
    void of_generatesCodeFromComponents() {
        EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "CPI", ReleaseFrequency.MONTHLY, IndicatorUnit.PERCENT);

        assertThat(code.indicatorCode()).isEqualTo("US_CPI_MONTHLY");
        assertThat(code.country()).isEqualTo("US");
        assertThat(code.type()).isEqualTo("CPI");
        assertThat(code.frequency()).isEqualTo(ReleaseFrequency.MONTHLY);
        assertThat(code.unit()).isEqualTo(IndicatorUnit.PERCENT);
    }

    @Test
    @DisplayName("of — 다른 인수 조합에서도 올바른 코드 생성")
    void of_differentArgs_generatesCorrectCode() {
        EconomicIndicatorCode code = EconomicIndicatorCode.of("KR", "GDP", ReleaseFrequency.QUARTERLY, IndicatorUnit.INDEX);

        assertThat(code.indicatorCode()).isEqualTo("KR_GDP_QUARTERLY");
    }
}
