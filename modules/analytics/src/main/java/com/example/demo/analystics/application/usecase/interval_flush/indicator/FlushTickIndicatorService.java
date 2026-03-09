package com.example.demo.analystics.application.usecase.interval_flush.indicator;

import com.example.demo.analystics.application.kernel.base.DispatchIndicatorManager;
import com.example.demo.analystics.application.usecase.base.interval_flush.FlushIndicatorService;
import com.example.demo.analystics.domain.domain.key.TickKey;
import org.springframework.stereotype.Component;

@Component
public class FlushTickIndicatorService extends FlushIndicatorService {

    public FlushTickIndicatorService(DispatchIndicatorManager<TickKey,?,?, ?> core) {
        super(core);
    }
}
