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
        return buffer.values().stream().toList();
    }}
