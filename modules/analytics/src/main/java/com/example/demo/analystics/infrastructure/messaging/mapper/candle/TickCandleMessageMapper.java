package com.example.demo.analystics.infrastructure.messaging.mapper.candle;

import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class TickCandleMessageMapper implements DomainToMessage<TickCloseCandle, TickCandleMessage> {
    @Override
    public TickCandleMessage toMessage(TickCloseCandle tickCloseCandle) {
        return new TickCandleMessage(
                valueOf(tickCloseCandle.marketCodeId()),
                tickCloseCandle.interval().getPeriod(),
                valueOf(tickCloseCandle.open()),
                valueOf(tickCloseCandle.high()),
                valueOf(tickCloseCandle.low()),
                valueOf(tickCloseCandle.close()),
                valueOf(tickCloseCandle.times().bucketOpenTs()),
                valueOf(tickCloseCandle.times().bucketCloseTs()),
                valueOf(tickCloseCandle.times().observeOpenTs()),
                valueOf(tickCloseCandle.times().observeCloseTs())
        );
    }

    private String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
