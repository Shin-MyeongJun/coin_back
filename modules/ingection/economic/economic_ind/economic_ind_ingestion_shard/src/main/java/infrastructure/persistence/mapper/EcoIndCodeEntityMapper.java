package infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.DomainToEntity;
import domain.EconomicIndicatorCode;
import domain.enums.IndicatorUnit;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcoIndCodeEntityMapper implements DomainToEntity<EconomicIndicatorCode,EcoIndCodeEntity> {

    @Override
    public EcoIndCodeEntity toEntity(EconomicIndicatorCode eic) {
        return EcoIndCodeEntity.builder()
                .indicatorCode(eic.indicatorCode())
                .type(eic.type())
                .country(eic.country())
                .frequency(eic.frequency())
                .unit(IndicatorUnit.fromValue(eic.unit()))
                .build();
    }
}
