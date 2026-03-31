package com.example.demo.analystics.domain.partition_registry;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.factory.indicator.value.PremiumIndicatorFactory;
import com.example.demo.analystics.domain.service.ClosingData;
import com.example.demo.analystics.domain.store.candle.PremiumCandleStore;
import com.example.demo.analystics.domain.store.indicator.PremiumIndicatorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PremiumPartitionRegistry implements PartitionLifecycle {

    private final Map<Integer, PremiumCandleStore> candleStores = new ConcurrentHashMap<>();
    private final Map<Integer, PremiumIndicatorStore> indicatorStores = new ConcurrentHashMap<>();

    private final ClosingData<PremiumCandle, PremiumCloseCandle> candleCloser;
    private final PremiumIndicatorFactory indicatorFactory;
    private final ClosingData<PremiumIndicator, PremiumCloseIndicator> indicatorCloser;

    public PremiumPartitionRegistry(
            ClosingData<PremiumCandle, PremiumCloseCandle> candleCloser,
            PremiumIndicatorFactory indicatorFactory,
            ClosingData<PremiumIndicator, PremiumCloseIndicator> indicatorCloser) {
        this.candleCloser = candleCloser;
        this.indicatorFactory = indicatorFactory;
        this.indicatorCloser = indicatorCloser;
    }

    @Override
    public void assignPartition(int partitionId) {
        candleStores.put(partitionId, new PremiumCandleStore(candleCloser));
        indicatorStores.put(partitionId, new PremiumIndicatorStore(indicatorFactory, indicatorCloser));
    }

    @Override
    public void revokePartitions(Collection<Integer> partitionIds) {
        partitionIds.forEach(id -> {
            candleStores.remove(id);
            indicatorStores.remove(id);
        });
    }

    public void restoreCandles(int partitionId, Map<Interval, List<PremiumCandle>> snapshot) {
        if (!candleStores.containsKey(partitionId)) assignPartition(partitionId);
        candleStores.get(partitionId).assign(snapshot);
    }

    public void restoreIndicators(int partitionId, Map<Interval, List<PremiumIndicator>> snapshot) {
        if (!indicatorStores.containsKey(partitionId)) assignPartition(partitionId);
        indicatorStores.get(partitionId).assign(snapshot);
    }

    public void update(int partitionId, PremiumKey key, BigDecimal val) {
        PremiumCandleStore cs = candleStores.get(partitionId);
        PremiumIndicatorStore is = indicatorStores.get(partitionId);
        if (cs == null || is == null) {
            log.warn("Partition {} has no store. Skipping update.", partitionId);
            return;
        }
        cs.update(key, val);
        is.update(key, val);
    }

    public List<PremiumCandle> getCandles(int partitionId, Interval interval) {
        PremiumCandleStore store = candleStores.get(partitionId);
        return store != null ? store.getCandles(interval) : List.of();
    }

    public List<PremiumIndicator> getIndicators(int partitionId, Interval interval) {
        PremiumIndicatorStore store = indicatorStores.get(partitionId);
        return store != null ? store.getIndicators(interval) : List.of();
    }

    public List<PremiumCloseCandle> flushCandles(Interval interval) {
        return candleStores.values().stream()
                .flatMap(store -> store.drain(interval).stream())
                .toList();
    }

    public List<PremiumCloseIndicator> flushIndicators(Interval interval) {
        return indicatorStores.values().stream()
                .flatMap(store -> store.drain(interval).stream())
                .toList();
    }

    public Collection<Integer> getActivePartitionIds() {
        return candleStores.keySet();
    }
}
