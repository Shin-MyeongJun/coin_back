package com.example.demo.ingestion.exchange.infrastruct.publisher;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MarketCodeRawPublisherTest {

    @Mock
    KafkaTemplate<String, MarketCodeRawMessage> kafkaTemplate;

    @InjectMocks
    MarketCodeRawPublisher sut;

    @Test
    @DisplayName("publish — 토픽 'ingestion-exchange.market-code-raw'로 메시지 전송")
    void publish_sendsToCorrectTopic() {
        // given
        MarketCodeRawMessage msg = new MarketCodeRawMessage("Upbit", "SPOT", "KR", "KRW", "BTC", "KRW-BTC");

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send("ingestion-exchange.market-code-raw", msg);
    }
}
