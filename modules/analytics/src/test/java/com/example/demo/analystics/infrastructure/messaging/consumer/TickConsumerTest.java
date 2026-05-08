package com.example.demo.analystics.infrastructure.messaging.consumer;

import com.example.demo.analystics.application.port.in.ParsingPriceValueUseCase;
import com.example.demo.analystics.application.port.in.TickAnalyticsUseCase;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.contracts.message.price_value.TickMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TickConsumerTest {

    @Mock
    ParsingPriceValueUseCase<TickMessage, TickKey, BigDecimal> parser;

    @Mock
    TickAnalyticsUseCase useCase;

    @InjectMocks
    TickConsumer sut;

    @Test
    @DisplayName("onMessage — parser로 key/value 추출 후 useCase.onData(partition, key, value) 호출")
    void onMessage_parsesAndDelegates() {
        // given
        TickMessage msg = new TickMessage(42L, new BigDecimal("50000"), new BigDecimal("50100"), 9_999L);
        ConsumerRecord<String, TickMessage> record = new ConsumerRecord<>(
                "market-data.tick", 3, 0L, "key", msg);

        TickKey expectedKey = new TickKey(42L);
        BigDecimal expectedVal = new BigDecimal("50000");

        given(parser.parseKey(msg)).willReturn(expectedKey);
        given(parser.parseValue(msg)).willReturn(expectedVal);

        // when
        sut.onMessage(record);

        // then
        then(useCase).should().onData(3, expectedKey, expectedVal);
    }

    @Test
    @DisplayName("onMessage — 서로 다른 파티션이면 partition ID 그대로 전달")
    void onMessage_partitionIdPassedThrough() {
        // given
        TickMessage msg = new TickMessage(1L, new BigDecimal("100"), new BigDecimal("101"), 1L);
        ConsumerRecord<String, TickMessage> record = new ConsumerRecord<>(
                "market-data.tick", 7, 0L, "k", msg);

        TickKey key = new TickKey(1L);
        given(parser.parseKey(msg)).willReturn(key);
        given(parser.parseValue(msg)).willReturn(new BigDecimal("100"));

        // when
        sut.onMessage(record);

        // then
        then(useCase).should().onData(7, key, new BigDecimal("100"));
    }
}
