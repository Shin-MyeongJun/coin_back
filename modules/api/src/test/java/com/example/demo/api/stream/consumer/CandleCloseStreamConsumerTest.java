package com.example.demo.api.stream.consumer;

import com.example.demo.api.stream.sink.AnalyticsStream;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CandleCloseStreamConsumerTest {

    private final AnalyticsStream analyticsStream = new AnalyticsStream();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CandleCloseStreamConsumer sut;

    @BeforeEach
    void setUp() {
        sut = new CandleCloseStreamConsumer(analyticsStream, objectMapper);
    }

    @Test
    @DisplayName("onPremiumDetailCandle ???좏슚??JSON?대㈃ premiumDetailCandleSink濡?emit")
    void onPremiumDetailCandle_validJson_emitsToSink() throws Exception {
        // given
        PremiumDetailCandleMessage expected = message();
        String json = objectMapper.writeValueAsString(expected);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("analytics.premium-detail-candle", 0, 0, "key", json);

        List<PremiumDetailCandleMessage> received = new ArrayList<>();
        analyticsStream.premiumDetailCandleSink.asFlux().subscribe(received::add);

        // when
        sut.onPremiumDetailCandle(record);

        // then
        assertThat(received).hasSize(1);
        assertThat(received.get(0)).isEqualTo(expected);
    }

    @Test
    @DisplayName("onPremiumDetailCandle ??JSON ?뚯떛 ?ㅽ뙣 ???덉쇅 ?놁씠 warn 泥섎━")
    void onPremiumDetailCandle_invalidJson_doesNotThrow() {
        // given
        ConsumerRecord<String, String> record = new ConsumerRecord<>("analytics.premium-detail-candle", 0, 0, "key", "{invalid json}");

        // when / then
        assertThatCode(() -> sut.onPremiumDetailCandle(record)).doesNotThrowAnyException();
    }

    private static PremiumDetailCandleMessage message() {
        return new PremiumDetailCandleMessage(
                "BTC",
                "1",
                "2",
                "1m",
                "10",
                "100",
                "20",
                "200",
                "11",
                "110",
                "22",
                "220",
                "9",
                "90",
                "18",
                "180",
                "10.5",
                "105",
                "21",
                "210",
                "1710000000000",
                "1710000059999",
                "1710000000100",
                "1710000059900"
        );
    }
}
