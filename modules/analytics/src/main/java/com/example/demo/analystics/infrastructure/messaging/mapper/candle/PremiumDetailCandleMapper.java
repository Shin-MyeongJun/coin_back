package com.example.demo.analystics.infrastructure.messaging.mapper.candle;

import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailCandleMapper implements DomainToMessage<PremiumDetailCloseCandle, PremiumDetailCandleMessage> {
    @Override
    public PremiumDetailCandleMessage toMessage(PremiumDetailCloseCandle premiumDetailCloseCandle) {
        PremiumDetailValue open = premiumDetailCloseCandle.open();
        PremiumDetailValue high = premiumDetailCloseCandle.high();
        PremiumDetailValue low = premiumDetailCloseCandle.low();
        PremiumDetailValue close = premiumDetailCloseCandle.close();

        return new PremiumDetailCandleMessage(
                premiumDetailCloseCandle.symbol(),
                valueOf(premiumDetailCloseCandle.baseExchangeId()),
                valueOf(premiumDetailCloseCandle.compareExchangeId()),
                premiumDetailCloseCandle.interval().getPeriod(),
                valueOf(open.baseVal()),
                valueOf(open.baseQuoteVal()),
                valueOf(open.compareVal()),
                valueOf(open.compareQuoteVal()),
                valueOf(high.baseVal()),
                valueOf(high.baseQuoteVal()),
                valueOf(high.compareVal()),
                valueOf(high.compareQuoteVal()),
                valueOf(low.baseVal()),
                valueOf(low.baseQuoteVal()),
                valueOf(low.compareVal()),
                valueOf(low.compareQuoteVal()),
                valueOf(close.baseVal()),
                valueOf(close.baseQuoteVal()),
                valueOf(close.compareVal()),
                valueOf(close.compareQuoteVal()),
                valueOf(premiumDetailCloseCandle.times().bucketOpenTs()),
                valueOf(premiumDetailCloseCandle.times().bucketCloseTs()),
                valueOf(premiumDetailCloseCandle.times().observeOpenTs()),
                valueOf(premiumDetailCloseCandle.times().observeCloseTs())
        );
    }

    private String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
