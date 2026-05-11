package com.example.demo.api.stream.handler;

import com.example.demo.api.stream.sink.MarketDataStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumDetailSseHandlerTest {

    private final MarketDataStream marketDataStream = new MarketDataStream();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PremiumDetailSseHandler sut;

    @BeforeEach
    void setUp() {
        sut = new PremiumDetailSseHandler(marketDataStream, objectMapper);
    }

    @Test
    @DisplayName("subscribe ??premium-detail raw SseEmitter 諛섑솚")
    void subscribe_returnsNonNullEmitter() {
        // when
        SseEmitter emitter = sut.subscribe();

        // then
        assertThat(emitter).isNotNull();
    }
}
