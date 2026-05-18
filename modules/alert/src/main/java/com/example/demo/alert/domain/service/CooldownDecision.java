package com.example.demo.alert.domain.service;

public record CooldownDecision(
        boolean acquired,
        String key,
        int cooldownSec
) {
    public static CooldownDecision acquired(String key, int cooldownSec) {
        return new CooldownDecision(true, key, cooldownSec);
    }

    public static CooldownDecision rejected(String key, int cooldownSec) {
        return new CooldownDecision(false, key, cooldownSec);
    }
}
