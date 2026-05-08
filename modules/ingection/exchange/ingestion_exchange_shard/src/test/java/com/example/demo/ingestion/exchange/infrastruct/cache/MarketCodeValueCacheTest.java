package com.example.demo.ingestion.exchange.infrastruct.cache;

import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketCodeValueCacheTest {

    private final MarketCodeValueCache sut = new MarketCodeValueCache();

    @Test
    @DisplayName("put → get — 저장 후 동일 key로 조회 가능")
    void put_thenGet_returnsStoredValue() {
        // given
        MarketCodeKey key = new MarketCodeKey("Upbit", "KRW-BTC");
        MarketCodeValue value = new MarketCodeValue("SPOT", "KRW", "BTC");

        // when
        sut.put(key, value);
        MarketCodeValue result = sut.get(key);

        // then
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("get — 없는 key이면 null 반환")
    void get_missingKey_returnsNull() {
        // given
        MarketCodeKey key = new MarketCodeKey("Binance", "ETHUSDT");

        // when
        MarketCodeValue result = sut.get(key);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("put — 같은 key로 두 번 put하면 나중 값으로 덮어씀")
    void put_sameKeyTwice_overwritesValue() {
        // given
        MarketCodeKey key = new MarketCodeKey("Upbit", "KRW-BTC");
        MarketCodeValue first  = new MarketCodeValue("SPOT", "KRW", "BTC");
        MarketCodeValue second = new MarketCodeValue("SPOT", "KRW", "BITCOIN");

        // when
        sut.put(key, first);
        sut.put(key, second);

        // then
        assertThat(sut.get(key)).isEqualTo(second);
    }
}
