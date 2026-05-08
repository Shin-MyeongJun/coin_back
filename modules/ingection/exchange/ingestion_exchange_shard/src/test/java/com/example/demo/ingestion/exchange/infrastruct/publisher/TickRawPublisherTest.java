package com.example.demo.ingestion.exchange.infrastruct.publisher;

import com.example.demo.contracts.message.raw.TickRawMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TickRawPublisherTest {

    @Mock
    KafkaTemplate<String, TickRawMessage> kafkaTemplate;

    @InjectMocks
    TickRawPublisher sut;

    @Test
    @DisplayName("publish — 토픽 'ingestion-exchange.tick-raw'로 메시지 전송")
    void publish_sendsToCorrectTopic() {
        // given
        TickRawMessage msg = new TickRawMessage("BTCUSDT", "binance", "SPOT", "USDT", "BTC", "50000", "50100", 1_000L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send("ingestion-exchange.tick-raw", msg);
    }
}
