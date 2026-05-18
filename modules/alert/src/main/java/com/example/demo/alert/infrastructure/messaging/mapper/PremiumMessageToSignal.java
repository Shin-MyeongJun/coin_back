package com.example.demo.alert.infrastructure.messaging.mapper;

import com.example.demo.alert.domain.signal.PremiumSignal;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumMessageToSignal implements MessageToDomain<PremiumMessage, PremiumSignal> {
    @Override
    public PremiumSignal toDomain(PremiumMessage message) {
        return new PremiumSignal(
                message.symbol(),
                SignalValueSupport.midpoint(message.bid(), message.ask()),
                message.timestamp(),
                message.baseExchangeId(),
                message.compareExchangeId()
        );
    }
}
