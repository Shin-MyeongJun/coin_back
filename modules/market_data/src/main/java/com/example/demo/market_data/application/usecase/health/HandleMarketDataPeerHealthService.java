package com.example.demo.market_data.application.usecase.health;

import com.example.demo.market_data.application.port.in.HandleHealthDataUseCase;
import com.example.demo.market_data.application.port.out.EvictTickCachePort;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.application.port.out.PremiumMarketCodeRegistryPort;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HandleMarketDataPeerHealthService implements HandleHealthDataUseCase {

    private final EvictTickCachePort tickEvicter;
    private final GetCacheDataPort<Long, MarketCodeSnapShotVal> codeGetter;
    private final GetCacheDataPort<Long, ExchangeSnapShotVal> exchangeGetter;
    private final PremiumMarketCodeRegistryPort marketCodeRegistry;

    @Override
    public void handlePeerAllDead(String subType) {
        invalidate(subType);
    }

    @Override
    public void handlePeerRecovered(String subType) {
        // No eager recovery is needed. New ticks will repopulate the cache.
    }



    public void invalidate(String exchangeName) {
        String normalizedExchangeName = normalizeKey(exchangeName);
        if (normalizedExchangeName == null) {
            return;
        }

        for (Long code : marketCodeRegistry.getAll()) {
            if (belongsToExchange(code, normalizedExchangeName)) {
                tickEvicter.evict(code);
                marketCodeRegistry.remove(code);
            }
        }
    }

    private boolean belongsToExchange(Long marketCodeId, String exchangeName) {
        Optional<MarketCodeSnapShotVal> marketCode = codeGetter.get(marketCodeId);
        if (marketCode.isEmpty()) {
            return false;
        }

        Optional<ExchangeSnapShotVal> exchange = exchangeGetter.get(marketCode.get().exchangeId());
        if (exchange.isEmpty()) {
            return false;
        }

        String cachedExchangeName = normalizeKey(exchange.get().name());
        return exchangeName.equals(cachedExchangeName);
    }

    private String normalizeKey(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        return val.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }

}
