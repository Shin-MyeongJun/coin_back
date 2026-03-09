package com.example.demo.market_data.domain.service;

import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.domain.domain.PriceValue;
import com.example.demo.market_data.domain.domain.Tick;
import org.springframework.stereotype.Component;

@Component
public class PriceValueConverter {

    public PriceValue convert(Tick tick) {
        return new PriceValue(
                tick.bid(),
                tick.ask(),
                tick.timestamp()
        );
    }

    public PriceValue convert(Premium premium){
        return new PriceValue(
                premium.bid(),
                premium.ask(),
                premium.timestamp()
        );
    }

    public PriceValue convert(PremiumDetail premiumDetail){
        return new PriceValue(
                premiumDetail.bid(),
                premiumDetail.ask(),
                premiumDetail.timestamp()
        );
    }


}
