package com.example.demo.analystics.application.port.in;

import com.example.demo.analystics.domain.domain.key.DataKey;

public interface ParsingPriceValueUseCase<MESSAGE, KEY extends DataKey<KEY>,VAL extends Comparable<VAL> > {
    KEY parseKey(MESSAGE message);
    VAL parseValue(MESSAGE message);
}
