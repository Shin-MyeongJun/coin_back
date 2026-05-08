package com.example.demo.market_data.infrastructure.messaging.consumer;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.application.port.in.ConsumeRawValueUseCase;
import com.example.demo.market_data.domain.domain.Tick;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TickConsumerTest {

    @Mock MessageToDomain<TickRawMessage, Tick> messageMapper;
    @Mock ConsumeRawValueUseCase<Tick> useCase;

    TickConsumer sut;

    @BeforeEach
    void setUp() {
        sut = new TickConsumer(messageMapper, useCase);
    }

    private static TickRawMessage rawMessage() {
        return new TickRawMessage("BTC-KRW", "Upbit", "SPOT", "KRW", "BTC",
                "50000000", "50100000", 9_999L);
    }

    private static Tick tick(long id) {
        return new Tick(id, new BigDecimal("50000000"), new BigDecimal("50100000"), 9_999L);
    }

    private static ConsumerRecord<String, TickRawMessage> record(TickRawMessage msg) {
        return new ConsumerRecord<>("ingestion-exchange.tick-raw", 0, 0L, "key", msg);
    }

    @Test
    @DisplayName("onMessage — 매퍼가 유효한 Tick 반환 시 useCase.consumeValue(tick) 호출")
    void onMessage_validMessage_callsUseCase() {
        // given
        TickRawMessage msg = rawMessage();
        Tick tick = tick(1L);
        given(messageMapper.toDomain(msg)).willReturn(tick);

        // when
        sut.onMessage(record(msg));

        // then
        then(useCase).should().consumeValue(tick);
    }

    @Test
    @DisplayName("onMessage — 매퍼가 null 반환 시 useCase 미호출(조기 종료)")
    void onMessage_mapperReturnsNull_useCaseNotCalled() {
        // given
        TickRawMessage msg = rawMessage();
        given(messageMapper.toDomain(msg)).willReturn(null);

        // when
        sut.onMessage(record(msg));

        // then
        then(useCase).should(never()).consumeValue(null);
    }

    @Test
    @DisplayName("onMessage — 매퍼가 record.value() 를 인자로 받아 ��출됨")
    void onMessage_mapperCalledWithRecordValue() {
        // given
        TickRawMessage msg = rawMessage();
        Tick tick = tick(2L);
        given(messageMapper.toDomain(msg)).willReturn(tick);

        // when
        sut.onMessage(record(msg));

        // then
        then(messageMapper).should().toDomain(msg);
    }
}
