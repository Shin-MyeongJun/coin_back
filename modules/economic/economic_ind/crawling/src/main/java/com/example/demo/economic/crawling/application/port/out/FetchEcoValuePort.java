package com.example.demo.economic.crawling.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

public interface FetchEcoValuePort {
    Optional<BigDecimal> fetchCurrentValue(String indicatorType);

    interface ForInvesting extends FetchEcoValuePort {}
    interface ForYahoo extends FetchEcoValuePort {}
}
