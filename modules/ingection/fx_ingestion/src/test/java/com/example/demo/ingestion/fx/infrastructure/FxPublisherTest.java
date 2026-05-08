package com.example.demo.ingestion.fx.infrastructure;

import com.example.demo.contracts.message.fx.FxMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FxPublisherTest {

    @Mock
    KafkaTemplate<String, FxMessage> kafkaTemplate;

    @InjectMocks
    FxPublisher sut;

    @Test
    @DisplayName("publish — kafkaTemplate.send('ingestion-fx.fx', msg) 호출")
    void publish_sendsToCorrectTopic() {
        FxMessage msg = new FxMessage("USD/KRW", "USD", "KRW", new BigDecimal("1325.00"), 1_000L);

        sut.publish(msg);

        then(kafkaTemplate).should().send("ingestion-fx.fx", msg);
    }
}
