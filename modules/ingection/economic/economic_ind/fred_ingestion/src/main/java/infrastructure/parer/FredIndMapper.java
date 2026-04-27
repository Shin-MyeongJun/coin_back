package infrastructure.parer;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import domain.EconomicIndicatorCode;
import domain.EconomicIndicatorValue;
import domain.EconomicRawIndicator;
import domain.enums.ReleaseFrequency;
import infrastructure.dto.FredObservationResultDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FredIndMapper implements RawToDomain<FredObservationResultDto, EconomicRawIndicator> {

    @Override
    public EconomicRawIndicator toDomain(FredObservationResultDto raw, Map<String, String> args) {

        EconomicIndicatorCode code = EconomicIndicatorCode.of(
                args.get("country"),
                args.get("type"),
                raw.frequency(),
                raw.units()
        );
        EconomicIndicatorValue value = new EconomicIndicatorValue(
                raw.value(),
                raw.
        )

        return new EconomicRawIndicator(
                code,
                value
        );
    }
}
