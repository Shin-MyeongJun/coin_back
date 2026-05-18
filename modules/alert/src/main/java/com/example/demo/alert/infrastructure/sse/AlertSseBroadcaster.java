package com.example.demo.alert.infrastructure.sse;

import com.example.demo.alert.application.port.out.BroadcastAlertFiringPort;
import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.infrastructure.web.dto.AlertFiringResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertSseBroadcaster implements BroadcastAlertFiringPort {
    private final AlertSseRegistry registry;

    @Override
    public void broadcast(AlertFiring firing) {
        registry.broadcast(firing.userId(), AlertFiringResponse.from(firing));
    }
}
