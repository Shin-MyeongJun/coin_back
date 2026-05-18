package com.example.demo.alert.infrastructure.web.dto;

import com.example.demo.alert.domain.domain.AlertFiring;

import java.math.BigDecimal;

public record AlertFiringResponse(
        Long id,
        Long ruleId,
        String userId,
        String ruleLabel,
        String conditionText,
        BigDecimal observedValue,
        long firedAt
) {
    public static AlertFiringResponse from(AlertFiring firing) {
        return new AlertFiringResponse(
                firing.id(),
                firing.ruleId(),
                firing.userId(),
                firing.ruleLabel(),
                firing.conditionText(),
                firing.observedValue(),
                firing.firedAt()
        );
    }
}
