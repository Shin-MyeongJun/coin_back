package com.example.demo.market_data.application.usecase;

import com.example.demo.market_data.application.port.in.ConsumeMetaSnapUseCase;
import com.example.demo.market_data.application.port.out.LoadMetaSnapshotPort;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class InitializeServiceTest {

    @Mock
    LoadMetaSnapshotPort metaSnapshotLoader;

    @Mock
    ConsumeMetaSnapUseCase<ExchangeSnapShot> exchangeCacheInitializer;

    @Mock
    ConsumeMetaSnapUseCase<MarketCodeSnapShot> marketCodeCacheInitializer;

    @Test
    void initializesExchangeCacheBeforeMarketCodeCache() {
        ExchangeSnapShot exchange = new ExchangeSnapShot(1L, "Binance", "SPOT", "USDT", "ACTIVE");
        MarketCodeSnapShot marketCode = new MarketCodeSnapShot(10L, 1L, "BTC", "BTCUSDT");
        given(metaSnapshotLoader.loadExchanges()).willReturn(List.of(exchange));
        given(metaSnapshotLoader.loadMarketCodes()).willReturn(List.of(marketCode));
        InitializeService sut = new InitializeService(
                metaSnapshotLoader,
                exchangeCacheInitializer,
                marketCodeCacheInitializer
        );

        sut.run();

        InOrder ordered = inOrder(exchangeCacheInitializer, marketCodeCacheInitializer);
        ordered.verify(exchangeCacheInitializer).consumeMeta(exchange);
        ordered.verify(marketCodeCacheInitializer).consumeMeta(marketCode);
    }

    @Test
    void stopsStartupWhenBootstrapFails() {
        RuntimeException failure = new RuntimeException("db unavailable");
        given(metaSnapshotLoader.loadExchanges()).willThrow(failure);
        InitializeService sut = new InitializeService(
                metaSnapshotLoader,
                exchangeCacheInitializer,
                marketCodeCacheInitializer
        );

        assertThatThrownBy(sut::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("failed to initialize market metadata cache")
                .hasCause(failure);

        then(exchangeCacheInitializer).shouldHaveNoInteractions();
        then(marketCodeCacheInitializer).shouldHaveNoInteractions();
    }
}
