package com.example.demo.market_data.domain.domain;


import java.math.BigDecimal;

public record Premium  (

   String symbol,
   Long baseExchangeId,
   Long compareExchangeId,
   BigDecimal bid,
   BigDecimal ask,
   long timestamp
   ) implements BidAskQuote {}
