package com.example.demo.market_data.application.port.out;

import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;

import java.util.List;

public interface LoadMetaSnapshotPort {
    List<ExchangeSnapShot> loadExchanges();

    List<MarketCodeSnapShot> loadMarketCodes();
}
