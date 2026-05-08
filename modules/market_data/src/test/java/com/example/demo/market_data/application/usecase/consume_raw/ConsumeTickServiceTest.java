package com.example.demo.market_data.application.usecase.consume_raw;

import com.example.demo.market_data.application.port.in.CalPremiumUseCase;
import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.in.PublishPriceDataUseCase;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ConsumeTickServiceTest {

    @Mock PutCacheDataPort<Long, Tick> putter;
    @Mock ParsingValUseCase<Tick, Long> parser;
    @Mock PriceValueBuffer<Tick> buffer;
    @Mock CalPremiumUseCase calculator;
    @Mock GetCacheDataPort<Long, MarketCodeSnapShotVal> marketCodeCache; // 생성자 파라미터용 (로직 미사용)
    @Mock PublishPriceDataUseCase<Tick> publisher;

    ConsumeTickService sut;

    @BeforeEach
    void setUp() {
        sut = new ConsumeTickService(putter, parser, buffer, calculator, marketCodeCache, publisher);
    }

    private static Tick tick(long id) {
        return new Tick(id, new BigDecimal("50000"), new BigDecimal("50100"), 1_000L);
    }

    @Test
    @DisplayName("consumeValue — putter.put(key, tick) 호출: parser.getKey 반환값이 key로 전달됨")
    void consumeValue_callsPutterWithParsedKey() {
        // given
        Tick tick = tick(1L);
        given(parser.getKey(tick)).willReturn(1L);

        // when
        sut.consumeValue(tick);

        // then
        then(putter).should().put(1L, tick);
    }

    @Test
    @DisplayName("consumeValue — buffer.add(tick) 호출")
    void consumeValue_addsTickToBuffer() {
        // given
        Tick tick = tick(2L);
        given(parser.getKey(tick)).willReturn(2L);

        // when
        sut.consumeValue(tick);

        // then
        then(buffer).should().add(tick);
    }

    @Test
    @DisplayName("consumeValue — publisher.process(tick) 호출")
    void consumeValue_publishesTick() {
        // given
        Tick tick = tick(3L);
        given(parser.getKey(tick)).willReturn(3L);

        // when
        sut.consumeValue(tick);

        // then
        then(publisher).should().process(tick);
    }

    @Test
    @DisplayName("consumeValue — calculator.cal(key) 호출: parser.getKey 반환값이 key로 전달됨")
    void consumeValue_triggersCalPremiumWithParsedKey() {
        // given
        Tick tick = tick(4L);
        given(parser.getKey(tick)).willReturn(4L);

        // when
        sut.consumeValue(tick);

        // then
        then(calculator).should().cal(4L);
    }

    @Test
    @DisplayName("consumeValue — putter·buffer·publisher·calculator 모두 1회 호출(happy path)")
    void consumeValue_happyPath_allCollaboratorsInvokedOnce() {
        // given
        Tick tick = tick(5L);
        given(parser.getKey(tick)).willReturn(5L);

        // when
        sut.consumeValue(tick);

        // then
        then(putter).should().put(5L, tick);
        then(buffer).should().add(tick);
        then(publisher).should().process(tick);
        then(calculator).should().cal(5L);
    }
}
