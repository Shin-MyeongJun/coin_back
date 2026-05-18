package com.example.demo.alert.infrastructure.messaging.mapper;

import com.example.demo.alert.domain.signal.PremiumDetailSignal;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailMessageToSignal implements MessageToDomain<PremiumDetailMessage, PremiumDetailSignal> {
    @Override
    public PremiumDetailSignal toDomain(PremiumDetailMessage message) {
        return new PremiumDetailSignal(
                message.symbol(),
                SignalValueSupport.premiumDetailValue(
                        message.baseBid(),
                        message.baseAsk(),
                        message.baseQuoteVal(),
                        message.compareBid(),
                        message.compareAsk(),
                        message.compareQuoteVal()
                ),
                message.timestamp(),
                message.baseExchangeId(),
                message.compareExchangeId(),
                message.baseBid(),
                message.baseAsk(),
                message.compareBid(),
                message.compareAsk()
        );
    }
}
