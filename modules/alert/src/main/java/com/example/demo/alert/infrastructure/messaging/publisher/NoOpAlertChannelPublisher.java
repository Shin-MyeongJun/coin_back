package com.example.demo.alert.infrastructure.messaging.publisher;

import com.example.demo.alert.application.port.out.SendAlertChannelPort;
import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.domain.domain.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoOpAlertChannelPublisher implements SendAlertChannelPort {
    @Override
    public void send(AlertFiring firing, Channel channel) {
        // EMAIL and DISCORD delivery are intentionally out of MVP scope.
    }
}
