package com.example.demo.api.stream.consumer;

import com.example.demo.api.stream.sink.MarketDataStream;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TickStreamConsumerTest {

    private final MarketDataStream marketDataStream = new MarketDataStream();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private TickStreamConsumer sut;

    @BeforeEach
    void setUp() {
        sut = new TickStreamConsumer(marketDataStream, objectMapper);
    }

    @Test
    @DisplayName("onMessage — 유효한 JSON이면 tickSink로 emit")
    void onMessage_validJson_emitsToSink() throws Exception {
        // given
        TickMessage expected = new TickMessage(1L, new BigDecimal("50000"), new BigDecimal("50001"), 1_000L);
        String json = objectMapper.writeValueAsString(expected);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("market-data.tick", 0, 0, "key", json);

        List<TickMessage> received = new ArrayList<>();
        marketDataStream.tickSink.asFlux().subscribe(received::add);

        // when
        sut.onMessage(record);

        // then
        assertThat(received).hasSize(1);
        assertThat(received.get(0)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("onMessage — JSON 파싱 실패 시 예외 없이 warn 처리")
    void onMessage_invalidJson_doesNotThrow() {
        // given
        ConsumerRecord<String, String> record = new ConsumerRecord<>("market-data.tick", 0, 0, "key", "{invalid json}");

        // when / then
        assertThatCode(() -> sut.onMessage(record)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("onMessage — 여러 메시지 순서대로 emit")
    void onMessage_multipleMessages_emitsInOrder() throws Exception {
        // given
        TickMessage msg1 = new TickMessage(1L, new BigDecimal("100"), new BigDecimal("101"), 1_000L);
        TickMessage msg2 = new TickMessage(2L, new BigDecimal("200"), new BigDecimal("201"), 2_000L);

        List<TickMessage> received = new ArrayList<>();
        marketDataStream.tickSink.asFlux().subscribe(received::add);

        // when
        sut.onMessage(new ConsumerRecord<>("topic", 0, 0, "k1", objectMapper.writeValueAsString(msg1)));
        sut.onMessage(new ConsumerRecord<>("topic", 0, 1, "k2", objectMapper.writeValueAsString(msg2)));

        // then
        assertThat(received).hasSize(2);
        assertThat(received.get(0).marketCodeId()).isEqualTo(1L);
        assertThat(received.get(1).marketCodeId()).isEqualTo(2L);
    }
}
