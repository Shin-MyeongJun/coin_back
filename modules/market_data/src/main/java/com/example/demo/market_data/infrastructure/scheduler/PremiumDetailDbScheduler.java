package com.example.demo.market_data.infrastructure.scheduler;

import com.example.demo.market_data.application.port.in.FlushPriceValueBufferUseCase;
import com.example.demo.market_data.infrastructure.scheduler.base.PriceValueDbScheduler;
import org.springframework.stereotype.Component;


@Component
public class PremiumDetailDbScheduler extends PriceValueDbScheduler {
    public PremiumDetailDbScheduler(FlushPriceValueBufferUseCase.ForPremiumDetail useCase) {
        super(useCase);
    }


}
