package com.example.demo.analystics.application.port.in;

import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.contracts.message.price_value.TickMessage;

import java.math.BigDecimal;

public interface ParsingPriceValueUseCase<MESSAGE, KEY extends DataKey<KEY>,VAL extends Comparable<VAL> > {
    KEY parseKey(MESSAGE message);
    VAL parseValue(MESSAGE message);

    interface forTick extends ParsingPriceValueUseCase<TickMessage, TickKey, BigDecimal>{};
    interface forPremium extends ParsingPriceValueUseCase<PremiumMessage, PremiumKey, BigDecimal>{};
    interface forPremiumDetail extends ParsingPriceValueUseCase<PremiumDetailMessage, PremiumKey, PremiumDetailValue>{};
}
