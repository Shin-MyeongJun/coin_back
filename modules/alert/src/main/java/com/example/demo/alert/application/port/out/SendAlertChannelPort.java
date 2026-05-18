package com.example.demo.alert.application.port.out;

import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.domain.domain.Channel;

public interface SendAlertChannelPort {
    void send(AlertFiring firing, Channel channel);
}
