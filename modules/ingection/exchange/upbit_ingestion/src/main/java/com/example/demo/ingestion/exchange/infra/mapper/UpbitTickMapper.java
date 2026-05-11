package com.example.demo.ingestion.exchange.infra.mapper;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infre_exchange.upbit.dto.UpbitOrderbookDto;
import com.example.demo.ingestion.exchange.application.port.out.GetCachedDataPort;
import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpbitTickMapper implements DomainToMessage<UpbitOrderbookDto, TickRawMessage> {

    private final  String EXCHANGE  ="UPBIT";
    private final  String EXCHANGE_TYPE  ="SPOT";
    private final GetCachedDataPort.forMarketCode getter;

    @Override
    public TickRawMessage toMessage(UpbitOrderbookDto dto) {
        String quote = "UNKNOWN";
        String base = "UNKNOWN";
        MarketCodeKey key= new MarketCodeKey(
                EXCHANGE,
                dto.cd()
        );
        MarketCodeValue val = getter.get(key);
        if(val != null){
            quote = val.quote().toUpperCase();
            base = val.base().toUpperCase();
        }

        return new TickRawMessage(
                dto.cd(),
                EXCHANGE,
                EXCHANGE_TYPE,
                quote,
                base,
                dto.obu().getFirst().bp().toString(),
                dto.obu().getFirst().ap().toString(),
                System.currentTimeMillis()
        );
    }
}
