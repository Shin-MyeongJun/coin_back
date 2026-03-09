package com.example.demo.analystics.application.usecase.dispatcher.indicator;

import com.example.demo.analystics.application.kernel.base.DispatchIndicatorManager;
import com.example.demo.analystics.application.usecase.base.dispatcher.DispatchingIndicatorService;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

@Component
public class DispatchingTickIndicatorService extends DispatchingIndicatorService<TickKey> {
    public DispatchingTickIndicatorService(DispatchIndicatorManager<TickKey,?,?, ?> core) {
        super(core);
    }
}
