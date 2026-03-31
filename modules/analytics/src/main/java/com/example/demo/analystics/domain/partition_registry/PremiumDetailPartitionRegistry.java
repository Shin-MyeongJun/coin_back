package com.example.demo.analystics.domain.partition_registry;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.service.ClosingData;
import com.example.demo.analystics.domain.store.candle.PremiumDetailCandleStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PremiumDetailPartitionRegistry implements PartitionLifecycle {

    private final Map<Integer, PremiumDetailCandleStore> candleStores = new ConcurrentHashMap<>();
    private final ClosingData<PremiumDetailCandle, PremiumDetailCloseCandle> candleCloser;

    public PremiumDetailPartitionRegistry(
            ClosingData<PremiumDetailCandle, PremiumDetailCloseCandle> candleCloser) {
        this.candleCloser = candleCloser;
    }

    @Override
    public void assignPartition(int partitionId) {
        candleStores.put(partitionId, new PremiumDetailCandleStore(candleCloser));
    }

    @Override
    public void revokePartitions(Collection<Integer> partitionIds) {
        partitionIds.forEach(candleStores::remove);
    }

    public void restoreCandles(int partitionId, Map<Interval, List<PremiumDetailCandle>> snapshot) {
        if (!candleStores.containsKey(partitionId)) assignPartition(partitionId);
        candleStores.get(partitionId).assign(snapshot);
    }

    public void update(int partitionId, PremiumKey key, PremiumDetailValue val) {
        PremiumDetailCandleStore store = candleStores.get(partitionId);
        if (store == null) {
            log.warn("Partition {} has no store. Skipping update.", partitionId);
            return;
        }
        store.update(key, val);
    }

    public List<PremiumDetailCandle> getCandles(int partitionId, Interval interval) {
        PremiumDetailCandleStore store = candleStores.get(partitionId);
        return store != null ? store.getCandles(interval) : List.of();
    }

    public List<PremiumDetailCloseCandle> flushCandles(Interval interval) {
        return candleStores.values().stream()
                .flatMap(store -> store.drain(interval).stream())
                .toList();
    }

    public Collection<Integer> getActivePartitionIds() {
        return candleStores.keySet();
    }
}
