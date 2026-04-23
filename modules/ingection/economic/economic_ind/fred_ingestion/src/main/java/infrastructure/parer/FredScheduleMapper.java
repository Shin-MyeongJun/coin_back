package infrastructure.parer;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import domain.EconomicSchedule;
import infrastructure.dto.ReleaseDateDto;

import java.util.Map;

public class FredScheduleMapper implements RawToDomain<ReleaseDateDto, EconomicSchedule> {
    @Override
    public EconomicSchedule toDomain(ReleaseDateDto releaseDateDto, Map<String, String> args) {
        return new EconomicSchedule(


                releaseDateDto.getDate()
        );
    }
}
