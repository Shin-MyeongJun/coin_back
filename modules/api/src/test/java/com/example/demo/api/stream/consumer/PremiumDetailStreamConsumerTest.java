package com.example.demo.api.stream.consumer;

import com.example.demo.api.stream.sink.MarketDataStream;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
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

class PremiumDetailStreamConsumerTest {

    private final MarketDataStream marketDataStream = new MarketDataStream();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PremiumDetailStreamConsumer sut;

    @BeforeEach
    void setUp() {
        sut = new PremiumDetailStreamConsumer(marketDataStream, objectMapper);
    }

    @Test
    @DisplayName("onMessage ???좏슚??JSON?대㈃ premiumDetailSink濡?emit")
    void onMessage_validJson_emitsToSink() throws Exception {
        // given
        PremiumDetailMessage expected = message("BTC", 1_000L);
        String json = objectMapper.writeValueAsString(expected);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("market-data.premium-detail", 0, 0, "key", json);

        List<PremiumDetailMessage> received = new ArrayList<>();
        marketDataStream.premiumDetailSink.asFlux().subscribe(received::add);

        // when
        sut.onMessage(record);

        // then
        assertThat(received).hasSize(1);
        assertThat(received.get(0)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("onMessage ??JSON ?뚯떛 ?ㅽ뙣 ???덉쇅 ?놁씠 warn 泥섎━")
    void onMessage_invalidJson_doesNotThrow() {
        // given
        ConsumerRecord<String, String> record = new ConsumerRecord<>("market-data.premium-detail", 0, 0, "key", "{invalid json}");

        // when / then
        assertThatCode(() -> sut.onMessage(record)).doesNotThrowAnyException();
    }

    private static PremiumDetailMessage message(String symbol, long timestamp) {
        return new PremiumDetailMessage(
                symbol,
                1L,
                2L,
                new BigDecimal("100.00"),
                new BigDecimal("101.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("99.00"),
                new BigDecimal("100.00"),
                new BigDecimal("990.00"),
                timestamp
        );
    }
}
