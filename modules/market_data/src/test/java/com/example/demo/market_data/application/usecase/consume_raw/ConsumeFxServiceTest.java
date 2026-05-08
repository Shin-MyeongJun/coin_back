package com.example.demo.market_data.application.usecase.consume_raw;

import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.domain.domain.FxKey;
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
class ConsumeFxServiceTest {

    @Mock PutCacheDataPort<FxKey, Fx> putter;
    @Mock ParsingValUseCase<Fx, FxKey> parser;

    ConsumeFxService sut;

    @BeforeEach
    void setUp() {
        sut = new ConsumeFxService(putter, parser);
    }

    private static Fx fx(String base, String compare) {
        return new Fx(base, compare, new BigDecimal("1340"), 1_000L);
    }

    @Test
    @DisplayName("consumeValue — putter.put(key, fx) 호출: parser.getKey 반환값이 key로 전달됨")
    void consumeValue_callsPutterWithParsedKey() {
        // given
        Fx fx = fx("USD", "KRW");
        FxKey key = new FxKey("USD", "KRW");
        given(parser.getKey(fx)).willReturn(key);

        // when
        sut.consumeValue(fx);

        // then
        then(putter).should().put(key, fx);
    }

    @Test
    @DisplayName("consumeValue — 다른 통화 쌍에도 동일하게 적용됨")
    void consumeValue_differentCurrencyPair_callsPutterCorrectly() {
        // given
        Fx fx = fx("JPY", "KRW");
        FxKey key = new FxKey("JPY", "KRW");
        given(parser.getKey(fx)).willReturn(key);

        // when
        sut.consumeValue(fx);

        // then
        then(putter).should().put(key, fx);
    }
}
