package com.example.demo.ingestion.economic.economic_ind.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.economic.EconomicIndicatorMessage;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EcoIndMessageMapperTest {

    @Test
    @DisplayName("경제지표 도메인을 contracts message로 매핑한다")
    void mapsDomainToMessage() {
        EcoIndMessageMapper sut = new EcoIndMessageMapper();
        EconomicRawIndicator domain = new EconomicRawIndicator(
                EconomicIndicatorCode.of("US", "CPI", ReleaseFrequency.MONTHLY, IndicatorUnit.PERCENT),
                new EconomicIndicatorValue(new BigDecimal("3.5"), "2026-05-11", 1L, 2L)
        );

        EconomicIndicatorMessage message = sut.toMessage(domain);

        assertThat(message.indicatorCode()).isEqualTo("US_CPI_MONTHLY");
        assertThat(message.type()).isEqualTo("CPI");
        assertThat(message.country()).isEqualTo("US");
        assertThat(message.value()).isEqualByComparingTo("3.5");
        assertThat(message.observationDate()).isEqualTo("2026-05-11");
        assertThat(message.releaseTimestamp()).isEqualTo(1L);
        assertThat(message.timestamp()).isEqualTo(2L);
        assertThat(message.source()).isEqualTo("economic-ind");
    }
}
