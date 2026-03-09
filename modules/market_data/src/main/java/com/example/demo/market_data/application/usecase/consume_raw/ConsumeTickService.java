package com.example.demo.market_data.application.usecase.consume_raw;

import com.example.demo.market_data.application.port.in.CalPremiumUseCase;
import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.in.PublishPriceDataUseCase;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.application.usecase.base.ConsumeRawValueService;
import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import org.springframework.stereotype.Component;

@Component
public class ConsumeTickService extends ConsumeRawValueService<Long, Tick> {

    private final PriceValueBuffer<Tick> buffer;
    private  final CalPremiumUseCase calculator;
    private final PublishPriceDataUseCase<Tick> publisher;

    public ConsumeTickService(PutCacheDataPort<Long, Tick> putter, ParsingValUseCase<Tick,Long> parser, PriceValueBuffer<Tick> buffer, CalPremiumUseCase calculater, GetCacheDataPort<Long, MarketCodeSnapShotVal> maketCodeCache, PublishPriceDataUseCase<Tick> publisher) {
        super(putter, parser);
        this.buffer = buffer;
        this.calculator = calculater;
        this.publisher = publisher;
    }

    @Override
    public void consumeValue(Tick tick) {
        putter.put(parser.getKey(tick),tick);
        buffer.add(tick);
        publisher.process(tick);
        calculator.cal(parser.getKey(tick));
    }
}
