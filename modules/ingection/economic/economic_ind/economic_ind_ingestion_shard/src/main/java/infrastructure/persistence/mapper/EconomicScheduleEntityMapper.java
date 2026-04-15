package infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityMapping;
import domain.EconomicSchedule;
import infrastructure.cache.EcoIndCodeCache;
import infrastructure.persistence.entity.EconomicScheduleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EconomicScheduleEntityMapper
        implements EntityMapping<EconomicSchedule, EconomicScheduleEntity> {

    private final EcoIndCodeCache cache;

    @Override
    public EconomicScheduleEntity toEntity(EconomicSchedule es) {
        return EconomicScheduleEntity.builder()
                .indCodeId(cache.getId(es.getCode()))
                .releaseDate(es.getReleaseDate())
                .releaseCode(es.getReleaseCode())
                .status(es.getState())
                .fetchedAt(es.getFetchedAt())
                .build();
    }

    @Override
    public EconomicSchedule toDomain(EconomicScheduleEntity es) {
        return new EconomicSchedule(
                es.getReleaseCode(),
                cache.getIndCode(es.getIndCodeId()),
                es.getReleaseDate()
        );
    }
}
