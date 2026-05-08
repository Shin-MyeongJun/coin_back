package com.example.demo.meta_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.meta.MarketCodeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MarketCodePublisherTest {

    @Mock
    KafkaTemplate<String, MarketCodeMessage> kafkaTemplate;

    @InjectMocks
    MarketCodePublisher sut;

    @Test
    @DisplayName("publish — 토픽 'meta-data.market-code'로 메시지 전송")
    void publish_sendsToCorrectTopic() {
        // given
        MarketCodeMessage msg = new MarketCodeMessage(2L, 1L, "BTC", "KRW-BTC");

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send("meta-data.market-code", msg);
    }
}
