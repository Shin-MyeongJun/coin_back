package com.example.demo.market_data.application.usecase.consume_meta;

import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ConsumeMarketCodeServiceTest {

    @Mock PutCacheDataPort<Long, MarketCodeSnapShotVal> valPutter;
    @Mock PutCacheDataPort<MarketCodeSnapShotVal, Long> keyPutter;
    @Mock ParsingValUseCase<MarketCodeSnapShot, MarketCodeSnapShotVal> parser;

    ConsumeMarketCodeService sut;

    @BeforeEach
    void setUp() {
        sut = new ConsumeMarketCodeService(valPutter, keyPutter, parser);
    }

    private static MarketCodeSnapShot marketCode(long id, long exchangeId) {
        return new MarketCodeSnapShot(id, exchangeId, "BTC", "BTC-KRW");
    }

    private static MarketCodeSnapShotVal val(long exchangeId) {
        return new MarketCodeSnapShotVal(exchangeId, "BTC", "BTC-KRW");
    }

    @Test
    @DisplayName("consumeMeta — valPutter.put(id, val) 호출: id 기준 스냅샷 값 저장")
    void consumeMeta_callsValPutterWithIdAndParsedVal() {
        // given
        MarketCodeSnapShot snapshot = marketCode(100L, 10L);
        MarketCodeSnapShotVal val = val(10L);
        given(parser.getKey(snapshot)).willReturn(val);

        // when
        sut.consumeMeta(snapshot);

        // then
        then(valPutter).should().put(100L, val);
    }

    @Test
    @DisplayName("consumeMeta — keyPutter.put(val, id) 호출: val 기준 역방향 캐시 저장")
    void consumeMeta_callsKeyPutterWithValAndId() {
        // given
        MarketCodeSnapShot snapshot = marketCode(100L, 10L);
        MarketCodeSnapShotVal val = val(10L);
        given(parser.getKey(snapshot)).willReturn(val);

        // when
        sut.consumeMeta(snapshot);

        // then
        then(keyPutter).should().put(val, 100L);
    }

    @Test
    @DisplayName("consumeMeta — valPutter·keyPutter 모두 1회 호출(happy path)")
    void consumeMeta_happyPath_bothPuttersInvokedOnce() {
        // given
        MarketCodeSnapShot snapshot = marketCode(200L, 20L);
        MarketCodeSnapShotVal val = new MarketCodeSnapShotVal(20L, "ETH", "ETH-KRW");
        given(parser.getKey(snapshot)).willReturn(val);

        // when
        sut.consumeMeta(snapshot);

        // then
        then(valPutter).should().put(200L, val);
        then(keyPutter).should().put(val, 200L);
    }
}
