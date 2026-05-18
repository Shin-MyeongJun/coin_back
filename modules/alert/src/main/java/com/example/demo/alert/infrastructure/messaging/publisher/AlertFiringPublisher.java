package com.example.demo.alert.infrastructure.messaging.publisher;

import com.example.demo.alert.application.port.out.PublishAlertFiringPort;
import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.infrastructure.messaging.mapper.FiringToMessage;
import com.example.demo.contracts.message.alert.AlertFiringMessage;
import com.example.demo.contracts.topic.AlertTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertFiringPublisher implements PublishAlertFiringPort {
    private final KafkaTemplate<String, AlertFiringMessage> kafkaTemplate;
    private final FiringToMessage mapper;

    @Override
    public void publish(AlertFiring firing) {
        AlertFiringMessage message = mapper.toMessage(firing);
        kafkaTemplate.send(AlertTopics.ALERT_FIRING, message.extractKey(), message);
    }
}
