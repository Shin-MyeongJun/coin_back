package com.example.demo.market_data.infrastructure.query;

import com.example.demo.market_data.application.port.out.LoadMetaSnapshotPort;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.application.usecase.GetExchangeListUseCase;
import com.example.demo.meta_data_query.application.usecase.GetMarketCodeListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MetaDataQuerySnapshotAdapter implements LoadMetaSnapshotPort {

    private final GetExchangeListUseCase exchangeListUseCase;
    private final GetMarketCodeListUseCase marketCodeListUseCase;

    @Override
    public List<ExchangeSnapShot> loadExchanges() {
        return exchangeListUseCase.execute().stream()
                .map(this::toExchangeSnapshot)
                .toList();
    }

    @Override
    public List<MarketCodeSnapShot> loadMarketCodes() {
        return marketCodeListUseCase.execute().stream()
                .map(this::toMarketCodeSnapshot)
                .toList();
    }

    private ExchangeSnapShot toExchangeSnapshot(ExchangeView view) {
        return new ExchangeSnapShot(
                view.id(),
                view.name(),
                view.exchangeType(),
                view.quote(),
                view.status()
        );
    }

    private MarketCodeSnapShot toMarketCodeSnapshot(MarketCodeView view) {
        return new MarketCodeSnapShot(
                view.id(),
                view.exchangeId(),
                view.base(),
                view.tradingPair()
        );
    }
}
