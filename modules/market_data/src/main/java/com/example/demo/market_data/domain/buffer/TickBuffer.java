package com.example.demo.market_data.domain.buffer;

import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import com.example.demo.market_data.domain.domain.Tick;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TickBuffer implements PriceValueBuffer<Tick> {
    private final Map<Long, Tick> buffer = new ConcurrentHashMap<>();

    @Override
    public void add(Tick tick) {
        buffer.put(tick.marketCodeId() ,tick);
    }

    @Override
    public List<Tick> flush() {
        if (buffer.isEmpty()) return List.of();
        // snapshot → clear 순서로 race window 최소화.
        List<Tick> snapshot = List.copyOf(buffer.values());
        buffer.clear();
        return snapshot;
    }}
