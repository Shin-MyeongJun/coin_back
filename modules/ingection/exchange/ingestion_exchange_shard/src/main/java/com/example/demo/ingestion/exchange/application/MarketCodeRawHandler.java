package com.example.demo.ingestion.exchange.application;


import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.ingestion.exchange.application.port.in.GetMarketCodeRaw;
import com.example.demo.ingestion.exchange.application.port.in.HandleMetaRawUseCase;
import com.example.demo.ingestion.exchange.application.port.in.ParseMarketCodeRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.RawPublishPort;
import com.example.demo.ingestion.exchange.infrastruct.cache.MarketCodeValueCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketCodeRawHandler implements HandleMetaRawUseCase {

    private final GetMarketCodeRaw getter;
    private final MarketCodeValueCache register;
    private final RawPublishPort<MarketCodeRawMessage> publisher;
    private  final ParseMarketCodeRawUseCase parser;

    @Override
    public void process() {
        List<MarketCodeRawMessage> rms = getter.getAll();
        rms.forEach(
                ms->{
                   register.put(parser.getKey(ms),parser.getValue(ms));
                }
        );
        rms.forEach(publisher::publish);
    }
}
