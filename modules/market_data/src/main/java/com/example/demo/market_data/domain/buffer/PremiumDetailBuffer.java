package com.example.demo.market_data.domain.buffer;

import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.domain.domain.PremiumKey;
import com.example.demo.market_data.domain.service.PremiumKeyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PremiumDetailBuffer implements PriceValueBuffer<PremiumDetail> {
    private final PremiumKeyParser parser;
    private final Map<PremiumKey, PremiumDetail> buffer = new ConcurrentHashMap<>();

    @Override
    public void add(PremiumDetail premium) {
        buffer.put(parser.parse(premium),premium);
    }

    @Override
    public List<PremiumDetail> flush() {
        return buffer.values().stream().toList();
    }
}
