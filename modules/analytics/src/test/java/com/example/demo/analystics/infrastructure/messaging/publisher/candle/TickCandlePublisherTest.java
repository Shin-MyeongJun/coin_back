package com.example.demo.analystics.infrastructure.messaging.publisher.candle;

import com.example.demo.contracts.message.candle.TickCandleMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TickCandlePublisherTest {

    @Mock
    KafkaTemplate<String, TickCandleMessage> kafkaTemplate;

    @InjectMocks
    TickCandlePublisher sut;

    @Test
    @DisplayName("publish — 토픽 'analytics.tick-candle'로 메시지 전송")
    void publish_sendsToCorrectTopic() {
        // given
        TickCandleMessage msg = new TickCandleMessage(
                "1",
                "1m",
                "100.00",
                "110.00",
                "95.00",
                "105.00",
                "1710000000000",
                "1710000059999",
                "1710000000100",
                "1710000059900"
        );

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send("analytics.tick-candle", msg);
    }
}
