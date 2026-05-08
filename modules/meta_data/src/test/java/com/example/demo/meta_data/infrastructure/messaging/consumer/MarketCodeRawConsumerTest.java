package com.example.demo.meta_data.infrastructure.messaging.consumer;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.meta_data.application.port.in.ConsumeMetaRawUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MarketCodeRawConsumerTest {

    @Mock
    ConsumeMetaRawUseCase useCase;

    @InjectMocks
    MarketCodeRawConsumer sut;

    @Test
    @DisplayName("onMessage — record.value()를 useCase.handle에 전달")
    void onMessage_delegatesToUseCase() {
        // given
        MarketCodeRawMessage raw = new MarketCodeRawMessage(
                "Upbit", "SPOT", "KR", "KRW", "BTC", "KRW-BTC");
        ConsumerRecord<String, MarketCodeRawMessage> record = new ConsumerRecord<>(
                "ingestion-exchange.market-code-raw", 0, 0L, "key", raw);

        // when
        sut.onMessage(record);

        // then
        then(useCase).should().handle(raw);
    }
}
