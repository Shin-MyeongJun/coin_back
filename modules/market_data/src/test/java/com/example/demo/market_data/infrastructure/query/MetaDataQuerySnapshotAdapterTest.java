package com.example.demo.market_data.infrastructure.query;

import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.application.usecase.GetExchangeListUseCase;
import com.example.demo.meta_data_query.application.usecase.GetMarketCodeListUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MetaDataQuerySnapshotAdapterTest {

    @Mock
    GetExchangeListUseCase exchangeListUseCase;

    @Mock
    GetMarketCodeListUseCase marketCodeListUseCase;

    @Test
    void mapsExchangeViewsToMarketDataSnapshots() {
        given(exchangeListUseCase.execute()).willReturn(List.of(
                new ExchangeView(1L, "Binance", "SPOT", "USDT", "GLOBAL", "ACTIVE")
        ));
        MetaDataQuerySnapshotAdapter sut = new MetaDataQuerySnapshotAdapter(
                exchangeListUseCase,
                marketCodeListUseCase
        );

        List<ExchangeSnapShot> result = sut.loadExchanges();

        assertThat(result).containsExactly(
                new ExchangeSnapShot(1L, "Binance", "SPOT", "USDT", "ACTIVE")
        );
    }

    @Test
    void mapsMarketCodeViewsToMarketDataSnapshots() {
        given(marketCodeListUseCase.execute()).willReturn(List.of(
                new MarketCodeView(10L, 1L, "BTC", "USDT", "BTCUSDT")
        ));
        MetaDataQuerySnapshotAdapter sut = new MetaDataQuerySnapshotAdapter(
                exchangeListUseCase,
                marketCodeListUseCase
        );

        List<MarketCodeSnapShot> result = sut.loadMarketCodes();

        assertThat(result).containsExactly(
                new MarketCodeSnapShot(10L, 1L, "BTC", "BTCUSDT")
        );
    }
}
