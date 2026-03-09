package com.example.demo.market_data.application.usecase.batch_save;

import com.example.demo.market_data.application.port.in.FlushPriceValueBufferUseCase;
import com.example.demo.market_data.application.port.out.WritePriceValuePort;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.application.usecase.base.PersistPriceValueBatchUseCase;
import com.example.demo.market_data.domain.buffer.TickBuffer;
import com.example.demo.market_data.domain.domain.Tick;
import org.springframework.stereotype.Component;

@Component
public class PersistTickBatchUseCase extends PersistPriceValueBatchUseCase<Tick>
        implements FlushPriceValueBufferUseCase.ForTick{
    public PersistTickBatchUseCase(WritePriceValuePort<Tick> dbAdapter,
                                   WriteRedisLatestDataPort<Tick> redisAdapter,
                                   TickBuffer buffer) {
        super(dbAdapter,redisAdapter, buffer);
    }
}
