package com.example.demo.analystics.application.usecase.consume_market;

import com.example.demo.analystics.application.port.in.DispatchingDataUseCase;
import com.example.demo.analystics.application.usecase.base.ConsumeMarketDataService;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TickConsumeService extends ConsumeMarketDataService<TickKey, BigDecimal> {

    protected TickConsumeService(List<DispatchingDataUseCase<TickKey, BigDecimal>> dispatchers) {
        super(dispatchers);
    }
}
