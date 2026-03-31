package com.example.demo.analystics.domain.store.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.service.ClosingData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TickCandleStore {

    private final Map<Interval, Map<TickKey, TickCandle>> buffers = new ConcurrentHashMap<>();
    private final ClosingData<TickCandle, TickCloseCandle> closer;

    public TickCandleStore(ClosingData<TickCandle, TickCloseCandle> closer) {
        this.closer = closer;
        for (Interval interval : Interval.analyticsSupported()) {
            buffers.put(interval, new ConcurrentHashMap<>());
        }
    }

    // ── 실시간 데이터 수신 ──

    public void update(TickKey key, BigDecimal val) {
        for (Map<TickKey, TickCandle> buffer : buffers.values()) {
            TickCandle candle = buffer.computeIfAbsent(key, k -> new TickCandle(k, val));
            candle.update(val);
        }
    }

    // ── 복원 (Kafka 리밸런싱 시 Redis에서 읽어온 상태 주입) ──

    public void assign(Map<Interval, List<TickCandle>> snapshot) {
        snapshot.forEach((interval, candles) -> {
            Map<TickKey, TickCandle> buffer = buffers.get(interval);
            if (buffer == null) return;
            buffer.clear();
            candles.forEach(c -> buffer.put(c.getKey(), c));
        });
    }

    // ── 조회 (캐싱·상태저장용) ──

    public List<TickCandle> getCandles(Interval interval) {
        Map<TickKey, TickCandle> buffer = buffers.get(interval);
        if (buffer == null) return List.of();
        return new ArrayList<>(buffer.values());
    }

    // ── Flush (스케줄러가 호출, 닫힌 캔들을 반환하고 버퍼 초기화) ──

    public List<TickCloseCandle> drain(Interval interval) {
        Map<TickKey, TickCandle> buffer = buffers.get(interval);
        if (buffer == null) return List.of();

        List<TickCloseCandle> result = new ArrayList<>(buffer.size());
        // close → 변환 → clear
        buffer.values().forEach(TickCandle::close);
        buffer.values().forEach(c -> result.add(closer.toClose(c, interval)));
        buffer.clear();
        return result;
    }
}
