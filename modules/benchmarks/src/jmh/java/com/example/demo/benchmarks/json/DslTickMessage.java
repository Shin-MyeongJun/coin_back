package com.example.demo.benchmarks.json;

import com.dslplatform.json.CompiledJson;
import com.example.demo.contracts.message.price_value.TickMessage;

import java.math.BigDecimal;

@CompiledJson
record DslTickMessage(
        Long marketCodeId,
        BigDecimal bid,
        BigDecimal ask,
        Long timestamp
) {

    static DslTickMessage from(TickMessage message) {
        return new DslTickMessage(
                message.marketCodeId(),
                message.bid(),
                message.ask(),
                message.timestamp()
        );
    }

    TickMessage toTickMessage() {
        return new TickMessage(marketCodeId, bid, ask, timestamp);
    }
}
