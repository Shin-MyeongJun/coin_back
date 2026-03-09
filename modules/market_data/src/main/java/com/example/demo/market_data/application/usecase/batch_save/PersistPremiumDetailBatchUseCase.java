package com.example.demo.market_data.application.usecase.batch_save;

import com.example.demo.market_data.application.port.in.FlushPriceValueBufferUseCase;
import com.example.demo.market_data.application.port.out.WritePriceValuePort;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.application.usecase.base.PersistPriceValueBatchUseCase;
import com.example.demo.market_data.domain.buffer.PremiumDetailBuffer;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import org.springframework.stereotype.Component;

@Component
public class PersistPremiumDetailBatchUseCase extends PersistPriceValueBatchUseCase<PremiumDetail>
        implements FlushPriceValueBufferUseCase.ForPremiumDetail {
    public PersistPremiumDetailBatchUseCase(WritePriceValuePort<PremiumDetail> dbAdapter,
                                            WriteRedisLatestDataPort<PremiumDetail> redisAdapter,
                                            PremiumDetailBuffer buffer) {
        super(dbAdapter,redisAdapter, buffer);
    }
}
