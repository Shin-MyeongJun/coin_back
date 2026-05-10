package com.example.demo.market_data.application.usecase;

import com.example.demo.market_data.application.port.in.ConsumeMetaSnapUseCase;
import com.example.demo.market_data.application.port.in.InitializeMarketUseCase;
import com.example.demo.market_data.application.port.out.LoadMetaSnapshotPort;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializeService implements InitializeMarketUseCase {
    private final LoadMetaSnapshotPort metaSnapshotLoader;
    private final ConsumeMetaSnapUseCase<ExchangeSnapShot> exchangeCacheInitializer;
    private final ConsumeMetaSnapUseCase<MarketCodeSnapShot> marketCodeCacheInitializer;

    @Override
    public void run() {
        try {
            metaSnapshotLoader.loadExchanges()
                    .forEach(exchangeCacheInitializer::consumeMeta);
            metaSnapshotLoader.loadMarketCodes()
                    .forEach(marketCodeCacheInitializer::consumeMeta);
        } catch (Exception e) {
            log.error("failed to initialize market metadata cache. market_data startup will be stopped.", e);
            throw new IllegalStateException("failed to initialize market metadata cache", e);
        }
    }
}
