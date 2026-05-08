package com.example.demo.market_data.infrastructure.messaging.consumer;

import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.market_data.application.port.in.ConsumeRawValueUseCase;
import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.infrastructure.messaging.mapper.raw.FxMessageMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FxConsumerTest {

    @Mock FxMessageMapper messageMapper;
    @Mock ConsumeRawValueUseCase<Fx> useCase;

    FxConsumer sut;

    @BeforeEach
    void setUp() {
        sut = new FxConsumer(messageMapper, useCase);
    }

    private static FxMessage fxMessage() {
        return new FxMessage("USD/KRW", "USD", "KRW", new BigDecimal("1340"), 9_999L);
    }

    private static Fx fx() {
        return new Fx("USD", "KRW", new BigDecimal("1340"), 9_999L);
    }

    private static ConsumerRecord<String, FxMessage> record(FxMessage msg) {
        return new ConsumerRecord<>("ingestion-fx.fx", 0, 0L, "key", msg);
    }

    @Test
    @DisplayName("onMessage — 매퍼가 Fx를 반환하면 useCase.consumeValue(fx) 호출")
    void onMessage_validMessage_callsUseCase() {
        // given
        FxMessage msg = fxMessage();
        Fx fx = fx();
        given(messageMapper.toDomain(msg)).willReturn(fx);

        // when
        sut.onMessage(record(msg));

        // then
        then(useCase).should().consumeValue(fx);
    }

    @Test
    @DisplayName("onMessage — 매퍼가 record.value() 를 인자로 받아 호출됨")
    void onMessage_mapperCalledWithRecordValue() {
        // given
        FxMessage msg = fxMessage();
        given(messageMapper.toDomain(msg)).willReturn(fx());

        // when
        sut.onMessage(record(msg));

        // then
        then(messageMapper).should().toDomain(msg);
    }
}
