package com.example.demo.analystics.domain.partition_registry;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.factory.indicator.value.TickIndicatorFactory;
import com.example.demo.analystics.domain.service.ClosingData;
import com.example.demo.analystics.domain.store.candle.TickCandleStore;
import com.example.demo.analystics.domain.store.indicator.TickIndicatorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TickPartitionRegistry implements PartitionLifecycle {

    private final Map<Integer, TickCandleStore> candleStores = new ConcurrentHashMap<>();
    private final Map<Integer, TickIndicatorStore> indicatorStores = new ConcurrentHashMap<>();

    private final ClosingData<TickCandle, TickCloseCandle> candleCloser;
    private final TickIndicatorFactory indicatorFactory;
    private final ClosingData<TickIndicator, TickCloseIndicator> indicatorCloser;

    public TickPartitionRegistry(
            ClosingData<TickCandle, TickCloseCandle> candleCloser,
            TickIndicatorFactory indicatorFactory,
            ClosingData<TickIndicator, TickCloseIndicator> indicatorCloser) {
        this.candleCloser = candleCloser;
        this.indicatorFactory = indicatorFactory;
        this.indicatorCloser = indicatorCloser;
    }

    // ── PartitionLifecycle ──

    @Override
    public void assignPartition(int partitionId) {
        candleStores.put(partitionId, new TickCandleStore(candleCloser));
        indicatorStores.put(partitionId, new TickIndicatorStore(indicatorFactory, indicatorCloser));
    }

    @Override
    public void revokePartitions(Collection<Integer> partitionIds) {
        partitionIds.forEach(id -> {
            candleStores.remove(id);
            indicatorStores.remove(id);
        });
    }

    // ── 복원 (Redis → Registry) ──

    public void restoreCandles(int partitionId, Map<Interval, List<TickCandle>> snapshot) {
        TickCandleStore store = candleStores.get(partitionId);
        if (store == null) {
            assignPartition(partitionId);
            store = candleStores.get(partitionId);
        }
        store.assign(snapshot);
    }

    public void restoreIndicators(int partitionId, Map<Interval, List<TickIndicator>> snapshot) {
        TickIndicatorStore store = indicatorStores.get(partitionId);
        if (store == null) {
            assignPartition(partitionId);
            store = indicatorStores.get(partitionId);
        }
        store.assign(snapshot);
    }

    // ── 실시간 데이터 수신 ──

    public void update(int partitionId, TickKey key, BigDecimal val) {
        TickCandleStore candleStore = candleStores.get(partitionId);
        TickIndicatorStore indicatorStore = indicatorStores.get(partitionId);

        if (candleStore == null || indicatorStore == null) {
            log.warn("Partition {} has no store. Skipping update.", partitionId);
            return;
        }

        candleStore.update(key, val);
        indicatorStore.update(key, val);
    }

    // ── 조회 (캐싱·상태저장용) ──

    public List<TickCandle> getCandles(int partitionId, Interval interval) {
        TickCandleStore store = candleStores.get(partitionId);
        return store != null ? store.getCandles(interval) : List.of();
    }

    public List<TickIndicator> getIndicators(int partitionId, Interval interval) {
        TickIndicatorStore store = indicatorStores.get(partitionId);
        return store != null ? store.getIndicators(interval) : List.of();
    }

    // ── Flush (스케줄러 호출) ──

    public List<TickCloseCandle> flushCandles(Interval interval) {
        return candleStores.values().stream()
                .flatMap(store -> store.drain(interval).stream())
                .toList();
    }

    public List<TickCloseIndicator> flushIndicators(Interval interval) {
        return indicatorStores.values().stream()
                .flatMap(store -> store.drain(interval).stream())
                .toList();
    }

    // ── 파티션 ID 목록 (revoke 시 상태 저장용) ──

    public Collection<Integer> getActivePartitionIds() {
        return candleStores.keySet();
    }
}
