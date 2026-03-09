package com.example.demo.market_data.application.usecase.consume_meta;

import com.example.demo.market_data.application.port.in.ConsumeMetaSnapUseCase;
import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ConsumeExchangeService implements ConsumeMetaSnapUseCase<ExchangeSnapShot> {
    private final PutCacheDataPort<Long , ExchangeSnapShotVal> valPutter;
    private final PutCacheDataPort<ExchangeSnapShotVal,Long> keyPutter;
    private final ParsingValUseCase<ExchangeSnapShot,ExchangeSnapShotVal> parser;


    @Override
    public void consumeMeta(ExchangeSnapShot exchangeSnapShot) {
        ExchangeSnapShotVal val = parser.getKey(exchangeSnapShot);
        valPutter.put(exchangeSnapShot.id(),val);
        keyPutter.put(val,exchangeSnapShot.id());
    }
}
