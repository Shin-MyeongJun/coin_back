package com.example.demo.alert.infrastructure.messaging.mapper;

import com.example.demo.alert.domain.signal.TickSignal;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickMessageToSignal implements MessageToDomain<TickMessage, TickSignal> {
    @Override
    public TickSignal toDomain(TickMessage message) {
        return new TickSignal(
                String.valueOf(message.marketCodeId()),
                SignalValueSupport.midpoint(message.bid(), message.ask()),
                message.timestamp(),
                message.marketCodeId()
        );
    }
}
