package com.example.demo.analystics.application.usecase.dispatcher.indicator;

import com.example.demo.analystics.application.kernel.base.DispatchIndicatorManager;
import com.example.demo.analystics.application.usecase.base.dispatcher.DispatchingIndicatorService;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

@Component
public class DispatchingPremiumIndicatorService extends DispatchingIndicatorService<PremiumKey> {
    public DispatchingPremiumIndicatorService(DispatchIndicatorManager<PremiumKey,?,?, ?> core) {
        super(core);
    }
}
