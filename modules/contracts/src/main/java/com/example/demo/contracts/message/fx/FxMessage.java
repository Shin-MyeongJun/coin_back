package com.example.demo.contracts.message.fx;

import java.math.BigDecimal;

public record FxMessage(
        String fxPair,
        String base,
        String compare,
        BigDecimal val,
        long timestamp
        ) {
}
