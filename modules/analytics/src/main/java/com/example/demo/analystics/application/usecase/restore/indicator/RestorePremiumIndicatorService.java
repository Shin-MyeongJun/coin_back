package com.example.demo.analystics.application.usecase.restore.indicator;

import com.example.demo.analystics.application.port.out.MappingRecoverToStatePort;
import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.application.usecase.base.RestoreAnalyticsStateService;
import com.example.demo.analystics.domain.dispatch_manager.AnalyticsMangerController;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import org.springframework.stereotype.Component;

@Component
public class RestorePremiumIndicatorService extends RestoreAnalyticsStateService<PremiumKey,
        RecoveryIndicatorState,
        PremiumIndicator
        > {
    public RestorePremiumIndicatorService(ReadAnalyticsStatePort<PremiumKey, RecoveryIndicatorState> reader, MappingRecoverToStatePort<RecoveryIndicatorState, PremiumKey, PremiumIndicator> mapper, AnalyticsMangerController<?, ?, PremiumIndicator, ?> core) {
        super(reader, mapper, core);
    }
}
