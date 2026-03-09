package com.example.demo.analystics.infrastructure.parser;

import com.example.demo.analystics.application.port.in.ParsingPriceValueUseCase;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PremiumKeyParser implements ParsingPriceValueUseCase<PremiumMessage, PremiumKey, BigDecimal>{


    @Override
    public PremiumKey parseKey(PremiumMessage premiumMessage) {
        return new PremiumKey(
                premiumMessage.symbol(),
                premiumMessage.baseExchangeId(),
                premiumMessage.compareExchangeId()
        );
    }

    @Override
    public BigDecimal parseValue(PremiumMessage premiumMessage) {
        return premiumMessage.bid();
    }
}