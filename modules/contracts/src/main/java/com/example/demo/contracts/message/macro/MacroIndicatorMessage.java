package com.example.demo.contracts.message.macro;

import java.math.BigDecimal;

public record MacroIndicatorMessage(
        String symbol,
        BigDecimal price,
        long timestamp,
        String source
) {}
