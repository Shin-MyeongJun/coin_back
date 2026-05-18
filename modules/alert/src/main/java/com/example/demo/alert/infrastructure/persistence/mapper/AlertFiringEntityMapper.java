package com.example.demo.alert.infrastructure.persistence.mapper;

import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.infrastructure.persistence.entity.AlertFiringEntity;
import com.example.demo.infra_shard.persistence.EntityMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertFiringEntityMapper implements EntityMapping<AlertFiring, AlertFiringEntity> {
    @Override
    public AlertFiringEntity toEntity(AlertFiring domain) {
        return AlertFiringEntity.builder()
                .id(domain.id())
                .ruleId(domain.ruleId())
                .userId(domain.userId())
                .ruleLabel(domain.ruleLabel())
                .conditionText(domain.conditionText())
                .observedValue(domain.observedValue())
                .firedAt(domain.firedAt())
                .build();
    }

    @Override
    public AlertFiring toDomain(AlertFiringEntity entity) {
        return new AlertFiring(
                entity.getId(),
                entity.getRuleId(),
                entity.getUserId(),
                entity.getRuleLabel(),
                entity.getConditionText(),
                entity.getObservedValue(),
                entity.getFiredAt()
        );
    }
}
