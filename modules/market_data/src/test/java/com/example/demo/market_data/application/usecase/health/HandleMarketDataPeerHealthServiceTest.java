package com.example.demo.market_data.application.usecase.health;

import com.example.demo.market_data.application.port.out.EvictTickCachePort;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.application.port.out.PremiumMarketCodeRegistryPort;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class HandleMarketDataPeerHealthServiceTest {

    @Mock
    EvictTickCachePort tickEvicter;

    @Mock
    GetCacheDataPort<Long, MarketCodeSnapShotVal> codeGetter;

    @Mock
    GetCacheDataPort<Long, ExchangeSnapShotVal> exchangeGetter;

    @Mock
    PremiumMarketCodeRegistryPort marketCodeRegistry;

    @Test
    void evictsOnlyTicksThatBelongToDeadExchange() {
        given(marketCodeRegistry.getAll()).willReturn(Set.of(1L, 2L));
        given(codeGetter.get(1L)).willReturn(Optional.of(new MarketCodeSnapShotVal(10L, "BTC", "BTCUSDT")));
        given(codeGetter.get(2L)).willReturn(Optional.of(new MarketCodeSnapShotVal(20L, "BTC", "BTCUSDT")));
        given(exchangeGetter.get(10L)).willReturn(Optional.of(new ExchangeSnapShotVal("Binance", "SPOT", "USDT")));
        given(exchangeGetter.get(20L)).willReturn(Optional.of(new ExchangeSnapShotVal("Upbit", "SPOT", "KRW")));
        HandleMarketDataPeerHealthService sut = new HandleMarketDataPeerHealthService(
                tickEvicter,
                codeGetter,
                exchangeGetter,
                marketCodeRegistry
        );

        sut.handlePeerAllDead("binance");

        then(tickEvicter).should().evict(1L);
        then(marketCodeRegistry).should().remove(1L);
        then(tickEvicter).should(never()).evict(2L);
        then(marketCodeRegistry).should(never()).remove(2L);
    }

    @Test
    void ignoresBlankSubType() {
        HandleMarketDataPeerHealthService sut = new HandleMarketDataPeerHealthService(
                tickEvicter,
                codeGetter,
                exchangeGetter,
                marketCodeRegistry
        );

        sut.handlePeerAllDead(" ");

        then(marketCodeRegistry).shouldHaveNoInteractions();
        then(tickEvicter).shouldHaveNoInteractions();
    }

    @Test
    void recoveredDoesNotEagerlyRepopulateCache() {
        HandleMarketDataPeerHealthService sut = new HandleMarketDataPeerHealthService(
                tickEvicter,
                codeGetter,
                exchangeGetter,
                marketCodeRegistry
        );

        sut.handlePeerRecovered("binance");

        then(marketCodeRegistry).shouldHaveNoInteractions();
        then(tickEvicter).shouldHaveNoInteractions();
    }
}
