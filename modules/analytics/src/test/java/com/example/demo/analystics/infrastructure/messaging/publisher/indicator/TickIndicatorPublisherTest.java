package com.example.demo.analystics.infrastructure.messaging.publisher.indicator;

import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TickIndicatorPublisherTest {

    @Mock
    KafkaTemplate<String, TickIndicatorMessage> kafkaTemplate;

    @InjectMocks
    TickIndicatorPublisher sut;

    @Test
    @DisplayName("publish — 토픽 'analytics.tick-indicator'로 메시지 전송")
    void publish_sendsToCorrectTopic() {
        // given
        TickIndicatorMessage msg = new TickIndicatorMessage();

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send("analytics.tick-indicator", msg);
    }
}
