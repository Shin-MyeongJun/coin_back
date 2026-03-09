package com.example.demo.market_data.domain.domain;


import java.math.BigDecimal;

public record Tick(
        long marketCodeId,
        BigDecimal bid,
        BigDecimal ask,
        long timestamp
        ) implements BidAskQuote  {}



