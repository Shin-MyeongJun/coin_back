package com.example.demo.market_data.infrastructure.messaging.mapper.raw;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TickRawMessageMappingAdapterTest {

    @Mock
    GetCacheDataPort<ExchangeSnapShotVal, Long> exchangeIdCache;

    @Mock
    GetCacheDataPort<MarketCodeSnapShotVal, Long> marketCodeIdCache;

    @Test
    @DisplayName("raw tick 가격 문자열을 BigDecimal로 직접 보존한다")
    void mapsPriceStringsDirectlyToBigDecimal() {
        TickRawMessage raw = new TickRawMessage(
                "BTCUSDT", "binance", "SPOT", "USDT", "BTC",
                "50000.12345678", "50100.98765432", 1_000L);
        given(exchangeIdCache.get(new ExchangeSnapShotVal("binance", "SPOT", "USDT")))
                .willReturn(Optional.of(1L));
        given(marketCodeIdCache.get(new MarketCodeSnapShotVal(1L, "BTC", "BTCUSDT")))
                .willReturn(Optional.of(10L));
        TickRawMessageMappingAdapter sut = new TickRawMessageMappingAdapter(exchangeIdCache, marketCodeIdCache);

        Tick result = sut.toDomain(raw);

        assertThat(result.marketCodeId()).isEqualTo(10L);
        assertThat(result.bid()).isEqualByComparingTo("50000.12345678");
        assertThat(result.ask()).isEqualByComparingTo("50100.98765432");
        assertThat(result.timestamp()).isEqualTo(1_000L);
    }
}
