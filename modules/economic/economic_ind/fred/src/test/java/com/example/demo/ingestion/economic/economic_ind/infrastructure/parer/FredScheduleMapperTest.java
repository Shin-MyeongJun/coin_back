package com.example.demo.ingestion.economic.economic_ind.infrastructure.parer;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.config.FredProperties;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FredScheduleMapperTest {

    @Test
    @DisplayName("FRED release date를 tracked series 설정과 합쳐 발표 일정 도메인으로 매핑한다")
    void mapsReleaseDateWithTrackedSeriesMetadata() {
        FredProperties properties = new FredProperties();
        FredProperties.TrackedSeries series = new FredProperties.TrackedSeries();
        series.setCountry("US");
        series.setStandardIndName("CPI");
        series.setSeriesId("CPIAUCSL");
        series.setReleaseId(10);
        series.setFrequency("MONTHLY");
        properties.setTrackedSeries(List.of(series));
        ReleaseDateDto dto = new ReleaseDateDto();
        dto.setReleaseId(10);
        dto.setDate("2026-05-11");
        FredScheduleMapper sut = new FredScheduleMapper(properties);

        EconomicSchedule result = sut.toDomain(dto, Map.of());

        assertThat(result.getReleaseCode()).isEqualTo("US_CPI_MONTHLY_20260511");
        assertThat(result.getCode().indicatorCode()).isEqualTo("US_CPI_MONTHLY");
        assertThat(result.getCode().frequency()).isEqualTo(ReleaseFrequency.MONTHLY);
        assertThat(result.getReleaseDate()).isEqualTo(20260511L);
    }

    @Test
    @DisplayName("tracked series에 없는 release id는 저장 대상에서 제외할 수 있도록 null로 매핑한다")
    void unknownReleaseIdReturnsNull() {
        FredProperties properties = new FredProperties();
        properties.setTrackedSeries(List.of());
        ReleaseDateDto dto = new ReleaseDateDto();
        dto.setReleaseId(999);
        dto.setDate("2026-05-11");
        FredScheduleMapper sut = new FredScheduleMapper(properties);

        assertThat(sut.toDomain(dto, Map.of())).isNull();
    }
}
