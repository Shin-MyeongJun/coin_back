package com.example.demo.ingestion.exchange.infra;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infra_shard.messaging.mapper.RawToMessage;
import com.example.demo.infre_exchange.dto.stream.BinanceBookTickerDto;
import com.example.demo.infre_exchange.util.BinanceCustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class BinanceTickRawParsingAdapter implements RawToMessage<String,TickRawMessage> {

    private final String PARSE_TYPE ="binanceTick";
    private final DslJsonParserManager parser;
    private final DomainToMessage<BinanceBookTickerDto, TickRawMessage> mapper;

    @Override
    public TickRawMessage toMessage(String s,Map<String,String> args) {
        System.out.println(s);
        BinanceBookTickerDto dto =  BinanceCustomUtil.getData(parser.parse(PARSE_TYPE, s));
        return mapper.toMessage(dto);
    }


}
