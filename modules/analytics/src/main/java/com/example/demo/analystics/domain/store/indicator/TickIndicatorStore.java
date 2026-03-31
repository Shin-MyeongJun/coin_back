package com.example.demo.analystics.domain.store.indicator;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.factory.indicator.value.TickIndicatorFactory;
import com.example.demo.analystics.domain.service.ClosingData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TickIndicatorStore {

    private final Map<Interval, Map<TickKey, Map<IndicatorKey, TickIndicator>>> buffers = new ConcurrentHashMap<>();
    private final TickIndicatorFactory factory;
    private final ClosingData<TickIndicator, TickCloseIndicator> closer;

    public TickIndicatorStore(TickIndicatorFactory factory,
                              ClosingData<TickIndicator, TickCloseIndicator> closer) {
        this.factory = factory;
        this.closer = closer;
        for (Interval interval : Interval.analyticsSupported()) {
            buffers.put(interval, new ConcurrentHashMap<>());
        }
    }

    // ── 실시간 데이터 수신 ──

    public void update(TickKey key, BigDecimal val) {
        for (Map<TickKey, Map<IndicatorKey, TickIndicator>> buffer : buffers.values()) {
            Map<IndicatorKey, TickIndicator> indicators =
                    buffer.computeIfAbsent(key, k -> new ConcurrentHashMap<>(factory.createIndicators(k, val)));
            indicators.values().forEach(ind -> ind.update(val));
        }
    }

    // ── 복원 ──

    public void assign(Map<Interval, List<TickIndicator>> snapshot) {
        snapshot.forEach((interval, indicators) -> {
            Map<TickKey, Map<IndicatorKey, TickIndicator>> buffer = buffers.get(interval);
            if (buffer == null) return;
            buffer.clear();
            for (TickIndicator ind : indicators) {
                buffer.computeIfAbsent(ind.getDataKey(), k -> new ConcurrentHashMap<>())
                        .put(ind.getIndicatorKey(), ind);
            }
        });
    }

    // ── 조회 ──

    public List<TickIndicator> getIndicators(Interval interval) {
        Map<TickKey, Map<IndicatorKey, TickIndicator>> buffer = buffers.get(interval);
        if (buffer == null) return List.of();

        List<TickIndicator> result = new ArrayList<>();
        buffer.values().forEach(inner -> result.addAll(inner.values()));
        return result;
    }

    // ── Flush ──

    public List<TickCloseIndicator> drain(Interval interval) {
        Map<TickKey, Map<IndicatorKey, TickIndicator>> buffer = buffers.get(interval);
        if (buffer == null) return List.of();

        List<TickCloseIndicator> result = new ArrayList<>();

        buffer.forEach((key, indicators) -> {
            // close → 변환
            indicators.values().forEach(ind -> {
                ind.close();
                result.add(closer.toClose(ind, interval));
            });
            // open for next interval
            indicators.values().forEach(ind -> ind.open());
        });

        return result;
    }
}