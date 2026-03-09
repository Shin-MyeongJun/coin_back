package com.example.demo.analystics.application.usecase.consume_market;

import com.example.demo.analystics.application.port.in.DispatchingDataUseCase;
import com.example.demo.analystics.application.usecase.base.ConsumeMarketDataService;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PremiumConsumeService extends ConsumeMarketDataService<PremiumKey, BigDecimal> {
    protected PremiumConsumeService(List<DispatchingDataUseCase<PremiumKey, BigDecimal>> dispatchers) {
        super(dispatchers);
    }
}
