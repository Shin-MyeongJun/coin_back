package com.example.demo.alert.infrastructure.messaging.mapper;

import com.example.demo.alert.domain.signal.IndicatorSignal;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickIndicatorMessageToSignal implements MessageToDomain<TickIndicatorMessage, IndicatorSignal> {
    @Override
    public IndicatorSignal toDomain(TickIndicatorMessage message) {
        return new IndicatorSignal(
                message.marketCodeId(),
                SignalValueSupport.parseDecimalOrZero(message.value()),
                SignalValueSupport.parseLongOrZero(message.observeCloseTs()),
                "tick",
                message.interval(),
                message.type(),
                message.period(),
                null,
                null
        );
    }
}
