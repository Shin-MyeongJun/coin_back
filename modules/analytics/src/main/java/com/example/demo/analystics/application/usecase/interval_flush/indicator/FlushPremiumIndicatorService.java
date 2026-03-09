package com.example.demo.analystics.application.usecase.interval_flush.indicator;

import com.example.demo.analystics.application.kernel.base.DispatchIndicatorManager;
import com.example.demo.analystics.application.usecase.base.interval_flush.FlushIndicatorService;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import org.springframework.stereotype.Component;

@Component
public class FlushPremiumIndicatorService extends FlushIndicatorService {
    public FlushPremiumIndicatorService(DispatchIndicatorManager<PremiumKey,?,?, ?> core) {
        super(core);
    }
}