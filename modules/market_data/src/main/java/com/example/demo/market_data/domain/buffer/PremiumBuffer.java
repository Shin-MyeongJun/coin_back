package com.example.demo.market_data.domain.buffer;

import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.domain.PremiumKey;
import com.example.demo.market_data.domain.service.PremiumKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PremiumBuffer implements PriceValueBuffer<Premium> {

    private final PremiumKeyParser parser;
    private final Map<PremiumKey, Premium> buffer = new ConcurrentHashMap<>();

    @Override
    public void add(Premium premium) {
        buffer.put(parser.parse(premium),premium);
    }

    @Override
    public List<Premium> flush() {
        return buffer.values().stream().toList();
    }
}
