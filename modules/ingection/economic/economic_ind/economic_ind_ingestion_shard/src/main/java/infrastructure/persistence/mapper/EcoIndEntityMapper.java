package infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.DomainToEntity;
import domain.EconomicIndicatorCode;
import domain.EconomicIndicatorValue;
import domain.EconomicRawIndicator;
import infrastructure.cache.EcoIndCodeCache;
import infrastructure.persistence.entity.EcoIndEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcoIndEntityMapper implements DomainToEntity<EconomicRawIndicator,EcoIndEntity> {

    private final EcoIndCodeCache cache;

    @Override
    public EcoIndEntity toEntity(EconomicRawIndicator ei) {

        EconomicIndicatorCode code = ei.code();
        EconomicIndicatorValue value = ei.value();

        return EcoIndEntity.builder()
                .indCodeId(cache.getId(code))
                .value(value.value())
                .observationDate(Long.valueOf(value.observationDate()))
                .releaseDate(value.releaseDate())
                .timestamp(value.timestamp())
                .build();
    }
}
