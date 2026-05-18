package com.example.demo.alert.application.port.out;

import com.example.demo.alert.domain.domain.AlertRule;

public interface SaveAlertRulePort {
    AlertRule save(AlertRule alertRule);
}
