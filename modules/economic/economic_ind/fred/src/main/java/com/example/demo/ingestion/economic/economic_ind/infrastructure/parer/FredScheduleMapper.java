package com.example.demo.ingestion.economic.economic_ind.infrastructure.parer;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.config.FredProperties;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FredScheduleMapper implements RawToDomain<ReleaseDateDto, EconomicSchedule> {

    private final FredProperties properties;

    @Override
    public EconomicSchedule toDomain(ReleaseDateDto releaseDateDto, Map<String, String> args) {
        if (releaseDateDto == null) {
            return null;
        }
        FredProperties.TrackedSeries series = findTrackedSeries(releaseDateDto.getReleaseId());
        if (series == null) {
            return null;
        }

        String country = firstNonBlank(series.getCountry(), args == null ? null : args.get("country"), "US");
        String type = firstNonBlank(series.getStandardIndName(), series.getSeriesId());
        ReleaseFrequency frequency = ReleaseFrequency.fromValue(series.getFrequency());
        IndicatorUnit unit = IndicatorUnit.fromValue(series.getUnit());
        EconomicIndicatorCode code = EconomicIndicatorCode.of(country, type, frequency, unit);
        Long releaseDate = toReleaseDate(releaseDateDto.getDate());
        return new EconomicSchedule(code.indicatorCode() + "_" + releaseDate, code, releaseDate);
    }

    private FredProperties.TrackedSeries findTrackedSeries(Integer releaseId) {
        if (releaseId == null || properties.getTrackedSeries() == null) {
            return null;
        }
        return properties.getTrackedSeries().stream()
                .filter(series -> Objects.equals(series.getReleaseId(), releaseId))
                .findFirst()
                .orElse(null);
    }

    private Long toReleaseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return Long.valueOf(date.replace("-", ""));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
