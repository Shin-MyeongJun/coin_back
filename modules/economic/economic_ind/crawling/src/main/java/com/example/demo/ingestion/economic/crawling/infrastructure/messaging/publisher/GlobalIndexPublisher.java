package com.example.demo.ingestion.economic.crawling.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.macro.MacroIndicatorMessage;
import com.example.demo.ingestion.economic.crawling.application.port.out.PublishGlobalIndexPort;
import com.example.demo.ingestion.economic.crawling.domain.domain.GlobalIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalIndexPublisher implements PublishGlobalIndexPort {

    private static final String TOPIC = "crawling.global-ind";

    private final KafkaTemplate<String, MacroIndicatorMessage> kafkaTemplate;

    @Override
    public void publish(GlobalIndex index) {
        MacroIndicatorMessage message = new MacroIndicatorMessage(
                index.symbol(),
                index.price(),
                index.timestamp(),
                index.source()
        );
        kafkaTemplate.send(TOPIC, index.symbol(), message);
        log.debug("Published [{}] price={} source={}", index.symbol(), index.price(), index.source());
    }
}
