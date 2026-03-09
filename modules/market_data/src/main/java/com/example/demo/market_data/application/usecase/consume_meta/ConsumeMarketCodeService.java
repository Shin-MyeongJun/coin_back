package com.example.demo.market_data.application.usecase.consume_meta;

import com.example.demo.market_data.application.port.in.ConsumeMetaSnapUseCase;
import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsumeMarketCodeService implements ConsumeMetaSnapUseCase<MarketCodeSnapShot> {

    private final PutCacheDataPort<Long , MarketCodeSnapShotVal> valPutter;
    private final PutCacheDataPort<MarketCodeSnapShotVal,Long> keyPutter;
    private final ParsingValUseCase<MarketCodeSnapShot,MarketCodeSnapShotVal> parser;


    @Override
    public void consumeMeta(MarketCodeSnapShot marketCodeSnapShot) {
        MarketCodeSnapShotVal val = parser.getKey(marketCodeSnapShot);
        valPutter.put(marketCodeSnapShot.id(),val);
        keyPutter.put(val,marketCodeSnapShot.id());
    }


}
