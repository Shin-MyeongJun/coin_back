package com.example.demo.ingestion.fx.infrastructure;

import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.ingestion.fx.application.port.out.PublishFxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FxPublisher implements PublishFxPort {

    private final KafkaTemplate<String, FxMessage> kafkaTemplate;
    @Override
    public void publish(FxMessage fm) {
       kafkaTemplate.send("ingestion-fx.fx", fm);
    }
}
