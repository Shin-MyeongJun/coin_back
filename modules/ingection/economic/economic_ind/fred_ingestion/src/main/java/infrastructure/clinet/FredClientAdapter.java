package infrastructure.clinet;

import application.port.out.LoadRawIndDataPort;
import application.port.out.LoadRawIndSchedulePort;
import infrastructure.config.FredProperties;
import infrastructure.dto.FredObservationResultDto;
import infrastructure.dto.ReleaseDateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FredClientAdapter implements LoadRawIndSchedulePort<ReleaseDateDto>,LoadRawIndDataPort<FredObservationResultDto> {

    private final FredClient client;
    private final FredProperties properties;

    @Override
    public List<FredObservationResultDto> getRaws() {
        List<FredObservationResultDto> targets = properties.getTrackedSeries().stream()
                .map(data -> client.fetchLatestObservation(data.getSeriesId()))
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
