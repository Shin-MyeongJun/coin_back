package com.example.demo.market_data.application.usecase.base;

import com.example.demo.market_data.application.port.in.ConsumeRawValueUseCase;
import com.example.demo.market_data.application.port.in.ParsingValUseCase;
import com.example.demo.market_data.application.port.out.PutCacheDataPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ConsumeRawValueService<KEY,DOMAIN> implements ConsumeRawValueUseCase<DOMAIN> {
    protected final PutCacheDataPort<KEY,DOMAIN> putter;
    protected final ParsingValUseCase<DOMAIN,KEY> parser;
}
