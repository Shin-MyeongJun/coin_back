package com.example.demo.analystics.application.usecase.restore.indicator;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RestoreAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import org.springframework.stereotype.Component;

@Component
public class RestoreTickIndicatorService extends RestoreAnalyticsStateService<TickKey,
        RecoveryIndicatorState,
        TickIndicator
        > {
    public RestoreTickIndicatorService(ReadAnalyticsStatePort<TickKey, RecoveryIndicatorState> reader, MappingRecoverToStatePort<RecoveryIndicatorState, TickKey, TickIndicator> mapper, AnalyticsMangerController<?, ?, TickIndicator, ?> core) {
        super(reader, mapper, core);
    }
}
