package com.example.demo.analystics.infrastructure.parser;

import com.example.demo.analystics.application.port.in.ParsingPriceValueUseCase;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailKeyParser implements ParsingPriceValueUseCase<PremiumDetailMessage, PremiumKey, PremiumDetailValue > {


    @Override
    public PremiumKey parseKey(PremiumDetailMessage premiumDetailMessage) {
        return new PremiumKey(
                premiumDetailMessage.symbol(),
                premiumDetailMessage.baseExchangeId(),
                premiumDetailMessage.compareExchangeId()
        );
    }

    @Override
    public PremiumDetailValue parseValue(PremiumDetailMessage premiumDetailMessage) {
        return new PremiumDetailValue(
                premiumDetailMessage.baseBid(),
                premiumDetailMessage.baseQuoteVal(),
                premiumDetailMessage.compareAsk(),
                premiumDetailMessage.compareQuoteVal()
        );
    }
}
