package com.example.demo.alert.infrastructure.messaging.mapper;

import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.contracts.message.alert.AlertFiringMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FiringToMessage implements DomainToMessage<AlertFiring, AlertFiringMessage> {
    @Override
    public AlertFiringMessage toMessage(AlertFiring domain) {
        return new AlertFiringMessage(
                domain.ruleId(),
                domain.userId(),
                domain.ruleLabel(),
                domain.conditionText(),
                domain.observedValue(),
                domain.firedAt()
        );
    }
}
