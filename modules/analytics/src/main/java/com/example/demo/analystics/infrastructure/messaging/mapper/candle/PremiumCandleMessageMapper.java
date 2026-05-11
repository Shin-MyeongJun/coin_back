package com.example.demo.analystics.infrastructure.messaging.mapper.candle;

import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PremiumCandleMessageMapper implements DomainToMessage<PremiumCloseCandle, PremiumCandleMessage> {
    @Override
    public PremiumCandleMessage toMessage(PremiumCloseCandle premiumCloseCandle) {
        return new PremiumCandleMessage(
                premiumCloseCandle.symbol(),
                valueOf(premiumCloseCandle.baseExchangeId()),
                valueOf(premiumCloseCandle.compareExchangeId()),
                premiumCloseCandle.interval().getPeriod(),
                valueOf(premiumCloseCandle.open()),
                valueOf(premiumCloseCandle.high()),
                valueOf(premiumCloseCandle.low()),
                valueOf(premiumCloseCandle.close()),
                valueOf(premiumCloseCandle.times().bucketOpenTs()),
                valueOf(premiumCloseCandle.times().bucketCloseTs()),
                valueOf(premiumCloseCandle.times().observeOpenTs()),
                valueOf(premiumCloseCandle.times().observeCloseTs())
        );
    }

    private String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
