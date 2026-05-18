package com.example.demo.alert.application.port.out;

import com.example.demo.alert.domain.service.CooldownDecision;

public interface CooldownGuardPort {
    CooldownDecision tryAcquire(long ruleId, int cooldownSec);
}
