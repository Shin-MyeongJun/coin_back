package com.example.demo.ingestion.exchange.application;


import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.messaging.mapper.RawToMessage;
import com.example.demo.ingestion.exchange.application.port.in.HandleMarketRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.RawPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickRawHandler<RAW>  implements HandleMarketRawUseCase<RAW> {

    private final RawToMessage<RAW, TickRawMessage> parser;
    private final RawPublishPort<TickRawMessage> publisher;


    @Override
    public void process(RAW raw) {
        TickRawMessage tm = parser.toMessage(raw, null);
        publisher.publish(tm);
    }
}
