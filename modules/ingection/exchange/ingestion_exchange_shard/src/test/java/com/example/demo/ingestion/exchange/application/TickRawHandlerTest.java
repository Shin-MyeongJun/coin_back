package com.example.demo.ingestion.exchange.application;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.messaging.mapper.RawToMessage;
import com.example.demo.ingestion.exchange.application.port.out.RawPublishPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TickRawHandlerTest {

    @Mock
    RawToMessage<String, TickRawMessage> parser;

    @Mock
    RawPublishPort<TickRawMessage> publisher;

    @InjectMocks
    TickRawHandler<String> sut;

    @Test
    @DisplayName("process — parser.toMessage(raw, null) 결과를 publisher.publish에 전달")
    void process_parsesAndPublishes() {
        // given
        String raw = "{\"price\":\"50000\"}";
        TickRawMessage msg = new TickRawMessage(1L, "50000", "50100", 9_999L);
        given(parser.toMessage(raw, null)).willReturn(msg);

        // when
        sut.process(raw);

        // then
        then(publisher).should().publish(msg);
    }
}
