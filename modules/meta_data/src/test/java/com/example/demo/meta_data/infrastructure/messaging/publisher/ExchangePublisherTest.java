package com.example.demo.meta_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ExchangePublisherTest {

    @Mock
    KafkaTemplate<String, ExchangeMessage> kafkaTemplate;

    @InjectMocks
    ExchangePublisher sut;

    @Test
    @DisplayName("publish — 토픽 'meta-data.exchange'로 메시지 전송")
    void publish_sendsToCorrectTopic() {
        // given
        ExchangeMessage msg = new ExchangeMessage(1L, "Upbit", "SPOT", "KRW");

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send("meta-data.exchange", msg);
    }
}
