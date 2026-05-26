package com.example.demo.benchmarks.json;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;

@CompiledJson
public record TestDslTickMessage(
        Long marketCodeId,
        BigDecimal bid,
        BigDecimal ask,
        Long timestamp
) {
}
