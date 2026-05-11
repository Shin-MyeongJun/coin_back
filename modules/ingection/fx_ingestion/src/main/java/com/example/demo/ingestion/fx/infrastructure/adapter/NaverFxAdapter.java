package com.example.demo.ingestion.fx.infrastructure.adapter;


import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.ingestion.fx.application.port.in.IngestFxDataUseCase;
import com.example.demo.ingestion.fx.infrastructure.client.NaverRealTimeFxClient;
import com.example.demo.ingestion.fx.infrastructure.mapper.NaverRawFxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NaverFxAdapter implements IngestFxDataUseCase {
    private final NaverRawFxMapper mapper;
    private final NaverRealTimeFxClient client;

    public FxMessage get(String base, String quote){
        String raw = client.getRaw(base, quote).block();
        Map<String,String > args = new HashMap<>();
        args.put("base", base);
        args.put("compare", quote);
        return mapper.toMessage(raw, args);
    }
}
