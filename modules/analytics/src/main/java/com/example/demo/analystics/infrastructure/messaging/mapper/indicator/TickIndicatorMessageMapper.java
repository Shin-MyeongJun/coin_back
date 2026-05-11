package com.example.demo.analystics.infrastructure.messaging.mapper.indicator;

import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorMessageMapper implements DomainToMessage<TickCloseIndicator, TickIndicatorMessage> {
    @Override
    public TickIndicatorMessage toMessage(TickCloseIndicator tickCloseIndicator) {
        return new TickIndicatorMessage(
                valueOf(tickCloseIndicator.marketCodeId()),
                tickCloseIndicator.interval().getPeriod(),
                tickCloseIndicator.type().getName(),
                valueOf(tickCloseIndicator.period()),
                valueOf(tickCloseIndicator.value()),
                valueOf(tickCloseIndicator.times().bucketOpenTs()),
                valueOf(tickCloseIndicator.times().bucketCloseTs()),
                valueOf(tickCloseIndicator.times().observeOpenTs()),
                valueOf(tickCloseIndicator.times().observeCloseTs())
        );
    }

    private String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
