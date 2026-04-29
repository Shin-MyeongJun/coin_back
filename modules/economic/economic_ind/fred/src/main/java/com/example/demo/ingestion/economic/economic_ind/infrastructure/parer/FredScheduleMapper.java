package com.example.demo.ingestion.economic.economic_ind.infrastructure.parer;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FredScheduleMapper implements RawToDomain<ReleaseDateDto, EconomicSchedule> {
    @Override
    public EconomicSchedule toDomain(ReleaseDateDto releaseDateDto, Map<String, String> args) {
        return new EconomicSchedule(


                releaseDateDto.getDate()
        );
    }
}
