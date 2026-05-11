package com.example.demo.analystics.infrastructure.messaging.mapper.indicator;

import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorMessageMapper implements DomainToMessage<PremiumCloseIndicator, PremiumIndicatorMessage> {
    @Override
    public PremiumIndicatorMessage toMessage(PremiumCloseIndicator premiumCloseIndicator) {
        return new PremiumIndicatorMessage(
                premiumCloseIndicator.symbol(),
                valueOf(premiumCloseIndicator.baseExchangeId()),
                valueOf(premiumCloseIndicator.compareExchangeId()),
                premiumCloseIndicator.interval().getPeriod(),
                premiumCloseIndicator.type().getName(),
                valueOf(premiumCloseIndicator.period()),
                valueOf(premiumCloseIndicator.value()),
                valueOf(premiumCloseIndicator.times().bucketOpenTs()),
                valueOf(premiumCloseIndicator.times().bucketCloseTs()),
                valueOf(premiumCloseIndicator.times().observeOpenTs()),
                valueOf(premiumCloseIndicator.times().observeCloseTs())
        );
    }

    private String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
