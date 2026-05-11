package com.example.demo.ingestion.economic.economic_ind.infrastructure.parer;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.config.FredProperties;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.FredObservationResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FredIndMapper implements RawToDomain<FredObservationResultDto, EconomicRawIndicator> {

    private final FredProperties properties;

    @Override
    public EconomicRawIndicator toDomain(FredObservationResultDto raw, Map<String, String> args) {
        if (raw == null || raw.value() == null || raw.value().isBlank() || ".".equals(raw.value())) {
            return null;
        }

        FredProperties.TrackedSeries series = findTrackedSeries(raw.seriesId());
        String country = firstNonBlank(
                series == null ? null : series.getCountry(),
                args == null ? null : args.get("country"),
                "US"
        );
        String type = firstNonBlank(
                series == null ? null : series.getStandardIndName(),
                raw.seriesId()
        );
        ReleaseFrequency frequency = ReleaseFrequency.fromValue(firstNonBlank(
                raw.frequency(),
                series == null ? null : series.getFrequency()
        ));
        IndicatorUnit unit = IndicatorUnit.fromValue(firstNonBlank(
                raw.units(),
                series == null ? null : series.getUnit()
        ));

        EconomicIndicatorCode code = EconomicIndicatorCode.of(country, type, frequency, unit);
        long now = System.currentTimeMillis();
        EconomicIndicatorValue value = new EconomicIndicatorValue(
                new BigDecimal(raw.value()),
                raw.date(),
                now,
                now
        );

        return new EconomicRawIndicator(code, value);
    }

    private FredProperties.TrackedSeries findTrackedSeries(String seriesId) {
        if (properties.getTrackedSeries() == null) {
            return null;
        }
        return properties.getTrackedSeries().stream()
                .filter(series -> Objects.equals(series.getSeriesId(), seriesId))
                .findFirst()
                .orElse(null);
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
