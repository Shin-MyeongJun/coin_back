package com.example.demo.analystics.domain.store.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.service.ClosingData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PremiumCandleStore {

    private final Map<Interval, Map<PremiumKey, PremiumCandle>> buffers = new ConcurrentHashMap<>();
    private final ClosingData<PremiumCandle, PremiumCloseCandle> closer;

    public PremiumCandleStore(ClosingData<PremiumCandle, PremiumCloseCandle> closer) {
        this.closer = closer;
        for (Interval interval : Interval.analyticsSupported()) {
            buffers.put(interval, new ConcurrentHashMap<>());
        }
    }

    public void update(PremiumKey key, BigDecimal val) {
        for (Map<PremiumKey, PremiumCandle> buffer : buffers.values()) {
            PremiumCandle candle = buffer.computeIfAbsent(key, k -> new PremiumCandle(k, val));
            candle.update(val);
        }
    }

    public void assign(Map<Interval, List<PremiumCandle>> snapshot) {
        snapshot.forEach((interval, candles) -> {
            Map<PremiumKey, PremiumCandle> buffer = buffers.get(interval);
            if (buffer == null) return;
            buffer.clear();
            candles.forEach(c -> buffer.put(c.getKey(), c));
        });
    }

    public List<PremiumCandle> getCandles(Interval interval) {
        Map<PremiumKey, PremiumCandle> buffer = buffers.get(interval);
        if (buffer == null) return List.of();
        return new ArrayList<>(buffer.values());
    }

    public List<PremiumCloseCandle> drain(Interval interval) {
        Map<PremiumKey, PremiumCandle> buffer = buffers.get(interval);
        if (buffer == null) return List.of();

        List<PremiumCloseCandle> result = new ArrayList<>(buffer.size());
        buffer.values().forEach(PremiumCandle::close);
        buffer.values().forEach(c -> result.add(closer.toClose(c, interval)));
        buffer.clear();
        return result;
    }
}
