package com.example.demo.analystics.application.usecase.consume_market;

import com.example.demo.analystics.application.port.in.DispatchingDataUseCase;
import com.example.demo.analystics.application.usecase.base.ConsumeMarketDataService;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PremiumDetailConsumeService extends ConsumeMarketDataService<PremiumKey, PremiumDetailValue> {
    protected PremiumDetailConsumeService(List<DispatchingDataUseCase<PremiumKey, PremiumDetailValue>> dispatchers) {
        super(dispatchers);
    }
}
