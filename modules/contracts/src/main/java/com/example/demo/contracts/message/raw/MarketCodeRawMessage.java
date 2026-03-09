package com.example.demo.contracts.message.raw;

public record MarketCodeRawMessage (
    String exchangeName,
    String exchangeType,
    String country,
    String asset,
    String base,
    String tradingPair

){

}
