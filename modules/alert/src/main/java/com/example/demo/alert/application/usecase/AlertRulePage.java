package com.example.demo.alert.application.usecase;

import com.example.demo.alert.domain.domain.AlertRule;

import java.util.List;

public record AlertRulePage(
        List<AlertRule> items,
        int page,
        int size,
        long total
) {
}
