package com.example.demo.ingestion.economic.economic_ind.infrastructure.clinet;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.config.FredProperties;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.FredObservationResultDto;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FredClientAdapterTest {

    @Mock FredClient client;
    @Mock FredProperties properties;

    @InjectMocks
    FredClientAdapter sut;

    @Test
    @DisplayName("getRaws — trackedSeries 각각에 대해 fetchLatestObservation 호출")
    void getRaws_delegatesToClientForEachSeries() {
        FredProperties.TrackedSeries s1 = new FredProperties.TrackedSeries();
        s1.setSeriesId("CPIAUCSL");
        FredProperties.TrackedSeries s2 = new FredProperties.TrackedSeries();
        s2.setSeriesId("UNRATE");

        FredObservationResultDto dto1 = new FredObservationResultDto("CPIAUCSL", "2025-01-01", "3.5", "Percent", "Monthly");
        FredObservationResultDto dto2 = new FredObservationResultDto("UNRATE", "2025-01-01", "4.1", "Percent", "Monthly");

        given(properties.getTrackedSeries()).willReturn(List.of(s1, s2));
        given(client.fetchLatestObservation("CPIAUCSL")).willReturn(dto1);
        given(client.fetchLatestObservation("UNRATE")).willReturn(dto2);

        List<FredObservationResultDto> result = sut.getRaws();

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("getRaw — target seriesId로 fetchLatestObservation 위임")
    void getRaw_delegatesWithSeriesId() {
        FredObservationResultDto dto = new FredObservationResultDto("GDP", "2025-01-01", "25000", "Billions", "Quarterly");
        given(client.fetchLatestObservation("GDP")).willReturn(dto);

        FredObservationResultDto result = sut.getRaw("GDP");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("getRawSchedules — fetchReleaseDates 결과를 그대로 반환")
    void getRawSchedules_delegatesToClient() {
        ReleaseDateDto release = new ReleaseDateDto();
        release.setReleaseId(1);
        release.setDate("2025-06-01");
        given(client.fetchReleaseDates(any(), any())).willReturn(List.of(release));

        List<ReleaseDateDto> result = sut.getRawSchedules();

        assertThat(result).containsExactly(release);
    }
}
