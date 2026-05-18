package com.example.demo.alert.application.usecase;

import com.example.demo.alert.application.port.in.EvaluateMarketSignalUseCase;
import com.example.demo.alert.application.port.out.ActiveRuleCachePort;
import com.example.demo.alert.application.port.out.BroadcastAlertFiringPort;
import com.example.demo.alert.application.port.out.CooldownGuardPort;
import com.example.demo.alert.application.port.out.PublishAlertFiringPort;
import com.example.demo.alert.application.port.out.SaveAlertFiringPort;
import com.example.demo.alert.application.port.out.SendAlertChannelPort;
import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.domain.domain.AlertRule;
import com.example.demo.alert.domain.domain.Channel;
import com.example.demo.alert.domain.service.CooldownDecision;
import com.example.demo.alert.domain.service.RuleMatcher;
import com.example.demo.alert.domain.signal.MarketSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class AlertEvaluationService implements EvaluateMarketSignalUseCase {
    private final ActiveRuleCachePort activeRuleCachePort;
    private final CooldownGuardPort cooldownGuardPort;
    private final SaveAlertFiringPort saveAlertFiringPort;
    private final PublishAlertFiringPort publishAlertFiringPort;
    private final BroadcastAlertFiringPort broadcastAlertFiringPort;
    private final SendAlertChannelPort sendAlertChannelPort;
    private final RuleMatcher ruleMatcher;
    private final Clock clock;

    @Override
    @Transactional
    public void onSignal(MarketSignal signal) {
        for (AlertRule rule : activeRuleCachePort.findByTargetAndAsset(signal.targetType(), signal.assetSymbol())) {
            if (!ruleMatcher.matches(rule, signal)) {
                continue;
            }
            CooldownDecision decision = cooldownGuardPort.tryAcquire(rule.getId(), rule.getCooldownSec());
            if (!decision.acquired()) {
                continue;
            }
            AlertFiring saved = saveAlertFiringPort.save(toFiring(rule, signal));
            publishAlertFiringPort.publish(saved);
            if (rule.getChannels().contains(Channel.SSE)) {
                broadcastAlertFiringPort.broadcast(saved);
            }
            rule.getChannels().stream()
                    .filter(channel -> channel != Channel.SSE)
                    .forEach(channel -> sendAlertChannelPort.send(saved, channel));
        }
    }

    private AlertFiring toFiring(AlertRule rule, MarketSignal signal) {
        return new AlertFiring(
                null,
                rule.getId(),
                rule.getUserId(),
                rule.getLabel(),
                signal.targetType() + " " + signal.assetSymbol() + " " + rule.getCondition().asText(),
                signal.value(),
                clock.millis()
        );
    }
}
