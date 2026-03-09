package com.example.demo.market_data.domain.service;

import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.domain.domain.PremiumKey;
import org.springframework.stereotype.Component;

@Component
public class PremiumKeyParser {
    public PremiumKey parse(Premium pre){
        return new PremiumKey(
                pre.symbol(),
                pre.baseExchangeId(),
                pre.compareExchangeId()
        );
    }

    public PremiumKey parse(PremiumDetail pre){
        return new PremiumKey(
                pre.symbol(),
                pre.baseExchangeId(),
                pre.compareExchangeId()
        );
    }
}
