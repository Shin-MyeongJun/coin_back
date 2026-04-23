package infrastructure.parer;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import domain.EconomicIndicatorCode;
import domain.EconomicIndicatorValue;
import domain.EconomicRawIndicator;
import infrastructure.dto.FredObservationResultDto;

import java.util.Map;

public class FredIndMapper implements RawToDomain<FredObservationResultDto, EconomicRawIndicator> {

    @Override
    public EconomicRawIndicator toDomain(FredObservationResultDto raw, Map<String, String> args) {

        EconomicIndicatorCode code = EconomicIndicatorCode.of(
                raw.
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
