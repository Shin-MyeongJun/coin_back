package com.example.demo.contracts.message.raw;

public record TickRawMessage(
        String tradingPair,
        String exchange,
        String exchangeType,
        String quote,
        String base,

        String bid,
        String ask,

        Long timestamp

) { }
