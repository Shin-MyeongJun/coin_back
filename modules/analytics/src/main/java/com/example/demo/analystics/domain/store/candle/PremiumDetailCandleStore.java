package com.example.demo.analystics.domain.store.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.domain.service.ClosingData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PremiumDetailCandleStore {

    private final Map<Interval, Map<PremiumKey, PremiumDetailCandle>> buffers = new ConcurrentHashMap<>();
    private final ClosingData<PremiumDetailCandle, PremiumDetailCloseCandle> closer;

    public PremiumDetailCandleStore(ClosingData<PremiumDetailCandle, PremiumDetailCloseCandle> closer) {
        this.closer = closer;
        for (Interval interval : Interval.analyticsSupported()) {
            buffers.put(interval, new ConcurrentHashMap<>());
        }
    }

    public void update(PremiumKey key, PremiumDetailValue val) {
        for (Map<PremiumKey, PremiumDetailCandle> buffer : buffers.values()) {
            PremiumDetailCandle candle = buffer.computeIfAbsent(key, k -> new PremiumDetailCandle(k, val));
            candle.update(val);
        }
    }

    public void assign(Map<Interval, List<PremiumDetailCandle>> snapshot) {
        snapshot.forEach((interval, candles) -> {
            Map<PremiumKey, PremiumDetailCandle> buffer = buffers.get(interval);
            if (buffer == null) return;
            buffer.clear();
            candles.forEach(c -> buffer.put(c.getKey(), c));
        });
    }

    public List<PremiumDetailCandle> getCandles(Interval interval) {
        Map<PremiumKey, PremiumDetailCandle> buffer = buffers.get(interval);
        if (buffer == null) return List.of();
        return new ArrayList<>(buffer.values());
    }

    public List<PremiumDetailCloseCandle> drain(Interval interval) {
        Map<PremiumKey, PremiumDetailCandle> buffer = buffers.get(interval);
        if (buffer == null) return List.of();

        List<PremiumDetailCloseCandle> result = new ArrayList<>(buffer.size());
        buffer.values().forEach(PremiumDetailCandle::close);
        buffer.values().forEach(c -> result.add(closer.toClose(c, interval)));
        buffer.clear();
        return result;
    }
}
