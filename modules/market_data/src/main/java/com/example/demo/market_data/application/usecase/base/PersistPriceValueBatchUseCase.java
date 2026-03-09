package com.example.demo.market_data.application.usecase.base;

import com.example.demo.market_data.application.port.in.FlushPriceValueBufferUseCase;
import com.example.demo.market_data.application.port.out.WritePriceValuePort;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class PersistPriceValueBatchUseCase<DOMAIN>  implements FlushPriceValueBufferUseCase {

    private final WritePriceValuePort<DOMAIN> dbWriter;
    private final WriteRedisLatestDataPort<DOMAIN> redisWriter;
    private final PriceValueBuffer<DOMAIN> buffer;

    @Override
    public void flush() {
        List<DOMAIN> domains =buffer.flush();
        dbWriter.saveAll(domains);
        redisWriter.upsertAll(domains);
    }
}
