package com.example.demo.market_data.application.usecase.consume_meta;

import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ConsumeExchangeServiceTest {

    @Mock PutCacheDataPort<Long, ExchangeSnapShotVal> valPutter;
    @Mock PutCacheDataPort<ExchangeSnapShotVal, Long> keyPutter;
    @Mock ParsingValUseCase<ExchangeSnapShot, ExchangeSnapShotVal> parser;

    ConsumeExchangeService sut;

    @BeforeEach
    void setUp() {
        sut = new ConsumeExchangeService(valPutter, keyPutter, parser);
    }

    private static ExchangeSnapShot exchange(long id) {
        return new ExchangeSnapShot(id, "Upbit", "SPOT", "KRW", "ACTIVE");
    }

    private static ExchangeSnapShotVal val() {
        return new ExchangeSnapShotVal("Upbit", "SPOT", "KRW");
    }

    @Test
    @DisplayName("consumeMeta — valPutter.put(id, val) 호출: id 기준 스냅샷 값 저장")
    void consumeMeta_callsValPutterWithIdAndParsedVal() {
        // given
        ExchangeSnapShot snapshot = exchange(10L);
        ExchangeSnapShotVal val = val();
        given(parser.getKey(snapshot)).willReturn(val);

        // when
        sut.consumeMeta(snapshot);

        // then
        then(valPutter).should().put(10L, val);
    }

    @Test
    @DisplayName("consumeMeta — keyPutter.put(val, id) 호출: val 기준 역방향 캐시 저장")
    void consumeMeta_callsKeyPutterWithValAndId() {
        // given
        ExchangeSnapShot snapshot = exchange(10L);
        ExchangeSnapShotVal val = val();
        given(parser.getKey(snapshot)).willReturn(val);

        // when
        sut.consumeMeta(snapshot);

        // then
        then(keyPutter).should().put(val, 10L);
    }

    @Test
    @DisplayName("consumeMeta — valPutter·keyPutter 모두 1회 호출(happy path)")
    void consumeMeta_happyPath_bothPuttersInvokedOnce() {
        // given
        ExchangeSnapShot snapshot = exchange(20L);
        ExchangeSnapShotVal val = new ExchangeSnapShotVal("Binance", "SPOT", "USDT");
        given(parser.getKey(snapshot)).willReturn(val);

        // when
        sut.consumeMeta(snapshot);

        // then
        then(valPutter).should().put(20L, val);
        then(keyPutter).should().put(val, 20L);
    }
}
