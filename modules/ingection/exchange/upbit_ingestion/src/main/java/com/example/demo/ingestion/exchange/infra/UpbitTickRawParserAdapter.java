package com.example.demo.ingestion.exchange.infra;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infra_shard.messaging.mapper.RawToMessage;
import com.example.demo.infre_exchange.upbit.dto.UpbitOrderbookDto;
import lombok.RequiredArgsConstructor;
import okio.ByteString;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpbitTickRawParserAdapter implements RawToMessage<ByteString, TickRawMessage> {

    private final String PARSE_TYPE ="upbitOrderbook";
    private final DslJsonParserManager parser;
    private final DomainToMessage<UpbitOrderbookDto, TickRawMessage> mapper;

    @Override
    public TickRawMessage toMessage(ByteString byteString, Map<String, String> args) {
        UpbitOrderbookDto dto = parser.parse(PARSE_TYPE, byteString);
        return mapper.toMessage(dto);
    }
}
