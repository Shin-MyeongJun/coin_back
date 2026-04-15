package infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityMapping;
import domain.EconomicIndicatorCode;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcoIndCodeEntityMapper
        implements EntityMapping<EconomicIndicatorCode,EcoIndCodeEntity> {

    @Override
    public EcoIndCodeEntity toEntity(EconomicIndicatorCode eic) {
        return EcoIndCodeEntity.builder()
                .indicatorCode(eic.indicatorCode())
                .type(eic.type())
                .country(eic.country())
                .frequency(eic.frequency())
                .unit(eic.unit())
                .build();
    }

    @Override
    public EconomicIndicatorCode toDomain(EcoIndCodeEntity eic) {
        return  new EconomicIndicatorCode(
                eic.getIndicatorCode(),
                eic.getCountry(),
                eic.getType(),
                eic.getFrequency(),
                eic.getUnit()
        );
    }
}
