package com.example.demo.market_data.application.usecase.consume_raw;


import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.application.usecase.base.ConsumeRawValueService;
import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.domain.domain.FxKey;
import org.springframework.stereotype.Component;

@Component
public class ConsumeFxService extends ConsumeRawValueService<FxKey,Fx> {


    public ConsumeFxService(PutCacheDataPort<FxKey, Fx> putter, ParsingValUseCase<Fx, FxKey> parser) {
        super(putter, parser);
    }

    @Override
    public void consumeValue(Fx fx) {
        putter.put(parser.getKey(fx), fx);
    }
}
