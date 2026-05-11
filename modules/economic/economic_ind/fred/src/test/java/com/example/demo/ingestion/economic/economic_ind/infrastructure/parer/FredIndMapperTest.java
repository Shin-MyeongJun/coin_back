package com.example.demo.ingestion.economic.economic_ind.infrastructure.parer;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.config.FredProperties;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.FredObservationResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FredIndMapperTest {

    @Test
    @DisplayName("FRED observation을 tracked series 설정과 합쳐 경제지표 도메인으로 매핑한다")
    void mapsObservationWithTrackedSeriesMetadata() {
        FredProperties properties = new FredProperties();
        FredProperties.TrackedSeries series = new FredProperties.TrackedSeries();
        series.setCountry("US");
        series.setStandardIndName("CPI");
        series.setSeriesId("CPIAUCSL");
        series.setFrequency("MONTHLY");
        properties.setTrackedSeries(List.of(series));
        FredIndMapper sut = new FredIndMapper(properties);
        FredObservationResultDto raw = new FredObservationResultDto(
                "CPIAUCSL", "2026-05-11", "3.5", "Percent", null);

        EconomicRawIndicator result = sut.toDomain(raw, Map.of());

        assertThat(result.code().indicatorCode()).isEqualTo("US_CPI_MONTHLY");
        assertThat(result.code().country()).isEqualTo("US");
        assertThat(result.code().type()).isEqualTo("CPI");
        assertThat(result.code().frequency()).isEqualTo(ReleaseFrequency.MONTHLY);
        assertThat(result.code().unit()).isEqualTo(IndicatorUnit.PERCENT);
        assertThat(result.value().value()).isEqualByComparingTo("3.5");
        assertThat(result.value().observationDate()).isEqualTo("2026-05-11");
    }

    @Test
    @DisplayName("FRED 결측값은 저장 대상에서 제외할 수 있도록 null로 매핑한다")
    void missingObservationValueReturnsNull() {
        FredProperties properties = new FredProperties();
        properties.setTrackedSeries(List.of());
        FredIndMapper sut = new FredIndMapper(properties);
        FredObservationResultDto raw = new FredObservationResultDto(
                "CPIAUCSL", "2026-05-11", ".", null, null);

        assertThat(sut.toDomain(raw, Map.of())).isNull();
    }
}
