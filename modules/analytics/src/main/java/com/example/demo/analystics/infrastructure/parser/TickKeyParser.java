package com.example.demo.analystics.infrastructure.parser;

import com.example.demo.analystics.application.port.in.ParsingPriceValueUseCase;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.contracts.message.price_value.TickMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class TickKeyParser implements   ParsingPriceValueUseCase<TickMessage, TickKey, BigDecimal>{

    @Override
    public TickKey parseKey(TickMessage tickMessage) {
        return new TickKey(tickMessage.marketCodeId());
    }

    @Override
    public BigDecimal parseValue(TickMessage tickMessage) {
        return tickMessage.bid();
    }
}
