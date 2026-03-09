package com.example.demo.market_data.domain.domain;

import java.math.BigDecimal;

public interface BidAskQuote {
    BigDecimal bid();
    BigDecimal ask();
}
