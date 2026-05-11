package com.example.demo.ingestion.economic.economic_ind.infrastructure.clinet;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndSchedulePort;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.config.FredProperties;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.FredObservationResultDto;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FredClientAdapter implements LoadRawIndSchedulePort<ReleaseDateDto>,LoadRawIndDataPort<FredObservationResultDto> {

    private final FredClient client;
    private final FredProperties properties;

    @Override
    public List<FredObservationResultDto> getRaws() {
        List<FredObservationResultDto> targets = properties.getTrackedSeries().stream()
                .map(data -> client.fetchLatestObservation(data.getSeriesId()))
                .filter(Objects::nonNull)
                .toList();
        return targets;
    }

    @Override
    public FredObservationResultDto getRaw(String target) {
        return client.fetchLatestObservation(target);
    }

    @Override
    public List<ReleaseDateDto> getRawSchedules() {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis() + 86400000 ;
        return client.fetchReleaseDates(Long.toString(startTime),Long.toString(endTime));
    }
}
