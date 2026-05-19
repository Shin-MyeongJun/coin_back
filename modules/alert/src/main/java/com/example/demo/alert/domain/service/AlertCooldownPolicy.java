package com.example.demo.alert.domain.service;

import org.springframework.stereotype.Component;

@Component
public class AlertCooldownPolicy {

    public boolean isAllowed(Long lastFiredAt, int cooldownSec, long now) {
        if (lastFiredAt == null) {
            return true;
        }
        if (cooldownSec <= 0) {
            return true;
        }
        long elapsedMs = now - lastFiredAt;
        long cooldownMs = (long) cooldownSec * 1000L;
        return elapsedMs >= cooldownMs;
    }
}
