package com.example.demo.market_data.application.port.in;

import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;

public interface ConsumeMetaSnapUseCase<SNAP> {
    void consumeMeta(SNAP snap);

    interface ForMarketCode extends ConsumeMetaSnapUseCase<MarketCodeSnapShot> {}
    interface ForExchange extends ConsumeMetaSnapUseCase<ExchangeSnapShot> {}
}
