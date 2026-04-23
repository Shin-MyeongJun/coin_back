package infrastructure.clinet;

import application.port.out.FredClientPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import domain.EconomicRawIndicator;
import domain.EconomicSchedule;
import infrastructure.dto.FredObservationResultDto;
import infrastructure.dto.ReleaseDateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FredClientAdapter implements FredClientPort {

    private final FredClient client;
    private final RawToDomain<> scheduleMapper;

    @Override
    public List<EconomicSchedule> getSchedules(String realTimeStart, String realTimeEnd) {

        List<ReleaseDateDto> rawList = client.fetchReleaseDates(realTimeStart, realTimeEnd);

        return List.of();
    }

    @Override
    public List<EconomicRawIndicator> getRawIndicators() {
        return List.of();
    }

    @Override
    public EconomicRawIndicator getRawIndicator(String indicator) {
        FredObservationResultDto dto = client.fetchLatestObservation(indicator);
        return null;
    }
}
