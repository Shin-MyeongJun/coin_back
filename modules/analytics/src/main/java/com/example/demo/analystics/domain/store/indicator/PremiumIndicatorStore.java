package com.example.demo.analystics.domain.store.indicator;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.factory.indicator.value.PremiumIndicatorFactory;
import com.example.demo.analystics.domain.service.ClosingData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PremiumIndicatorStore {

    private final Map<Interval, Map<PremiumKey, Map<IndicatorKey, PremiumIndicator>>> buffers = new ConcurrentHashMap<>();
    private final PremiumIndicatorFactory factory;
    private final ClosingData<PremiumIndicator, PremiumCloseIndicator> closer;

    public PremiumIndicatorStore(PremiumIndicatorFactory factory,
                                 ClosingData<PremiumIndicator, PremiumCloseIndicator> closer) {
        this.factory = factory;
        this.closer = closer;
        for (Interval interval : Interval.analyticsSupported()) {
            buffers.put(interval, new ConcurrentHashMap<>());
        }
    }

    public void update(PremiumKey key, BigDecimal val) {
        for (Map<PremiumKey, Map<IndicatorKey, PremiumIndicator>> buffer : buffers.values()) {
            Map<IndicatorKey, PremiumIndicator> indicators =
                    buffer.computeIfAbsent(key, k -> new ConcurrentHashMap<>(factory.createIndicators(k, val)));
            indicators.values().forEach(ind -> ind.update(val));
        }
    }

    public void assign(Map<Interval, List<PremiumIndicator>> snapshot) {
        snapshot.forEach((interval, indicators) -> {
            Map<PremiumKey, Map<IndicatorKey, PremiumIndicator>> buffer = buffers.get(interval);
            if (buffer == null) return;
            buffer.clear();
            for (PremiumIndicator ind : indicators) {
                buffer.computeIfAbsent(ind.getDataKey(), k -> new ConcurrentHashMap<>())
                        .put(ind.getIndicatorKey(), ind);
            }
        });
    }

    public List<PremiumIndicator> getIndicators(Interval interval) {
        Map<PremiumKey, Map<IndicatorKey, PremiumIndicator>> buffer = buffers.get(interval);
        if (buffer == null) return List.of();

        List<PremiumIndicator> result = new ArrayList<>();
        buffer.values().forEach(inner -> result.addAll(inner.values()));
        return result;
    }

    public List<PremiumCloseIndicator> drain(Interval interval) {
        Map<PremiumKey, Map<IndicatorKey, PremiumIndicator>> buffer = buffers.get(interval);
        if (buffer == null) return List.of();

        List<PremiumCloseIndicator> result = new ArrayList<>();
        buffer.forEach((key, indicators) -> {
            indicators.values().forEach(ind -> {
                ind.close();
                result.add(closer.toClose(ind, interval));
            });
            indicators.values().forEach(PremiumIndicator::open);
        });
        return result;
    }
}
