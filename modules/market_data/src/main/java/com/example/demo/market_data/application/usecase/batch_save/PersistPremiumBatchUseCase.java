package com.example.demo.market_data.application.usecase.batch_save;

import com.example.demo.market_data.application.port.in.FlushPriceValueBufferUseCase;
import com.example.demo.market_data.application.port.out.WritePriceValuePort;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.application.usecase.base.PersistPriceValueBatchUseCase;
import com.example.demo.market_data.domain.buffer.PremiumBuffer;
import com.example.demo.market_data.domain.domain.Premium;
import org.springframework.stereotype.Component;


@Component
public class PersistPremiumBatchUseCase extends PersistPriceValueBatchUseCase<Premium>
        implements FlushPriceValueBufferUseCase.ForPremium {
    public PersistPremiumBatchUseCase(WritePriceValuePort<Premium> dbAdapter,
                                      WriteRedisLatestDataPort<Premium> redisAdapter,
                                      PremiumBuffer buffer) {
        super(dbAdapter,redisAdapter ,buffer);
    }
}
