package com.example.demo.alert.application.port.out;

import com.example.demo.alert.domain.domain.AlertFiring;

public interface BroadcastAlertFiringPort {
    void broadcast(AlertFiring firing);
}
