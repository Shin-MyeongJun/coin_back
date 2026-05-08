package com.example.demo.api.stream.handler;

import com.example.demo.api.stream.sink.MarketDataStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class TickSseHandlerTest {

    private final MarketDataStream marketDataStream = new MarketDataStream();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private TickSseHandler sut;

    @BeforeEach
    void setUp() {
        sut = new TickSseHandler(marketDataStream, objectMapper);
    }

    @Test
    @DisplayName("subscribe — marketCodeId 지정 시 SseEmitter 반환")
    void subscribe_withMarketCodeId_returnsNonNullEmitter() {
        // when
        SseEmitter emitter = sut.subscribe(100L);

        // then
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("subscribe — marketCodeId=null 이면 전체 구독 SseEmitter 반환")
    void subscribe_withNullMarketCodeId_returnsNonNullEmitter() {
        // when
        SseEmitter emitter = sut.subscribe(null);

        // then
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("subscribe — 여러 번 호출해도 각각 독립적인 SseEmitter 반환")
    void subscribe_calledMultipleTimes_returnsDistinctEmitters() {
        // when
        SseEmitter emitter1 = sut.subscribe(1L);
        SseEmitter emitter2 = sut.subscribe(2L);

        // then
        assertThat(emitter1).isNotSameAs(emitter2);
    }
}
