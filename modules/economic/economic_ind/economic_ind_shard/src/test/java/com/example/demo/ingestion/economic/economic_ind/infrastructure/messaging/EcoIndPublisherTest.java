package com.example.demo.ingestion.economic.economic_ind.infrastructure.messaging;

import com.example.demo.contracts.message.economic.EconomicIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EcoIndPublisherTest {

    @Mock
    KafkaTemplate<String, EconomicIndicatorMessage> kafkaTemplate;

    @Mock
    DomainToMessage<EconomicRawIndicator, EconomicIndicatorMessage> mapper;

    @Test
    @DisplayName("경제지표 이벤트를 indicatorCode key로 Kafka에 발행한다")
    void publishSendsMappedMessageWithIndicatorCodeKey() {
        EconomicRawIndicator domain = new EconomicRawIndicator(
                EconomicIndicatorCode.of("US", "CPI", ReleaseFrequency.MONTHLY, IndicatorUnit.PERCENT),
                new EconomicIndicatorValue(new BigDecimal("3.5"), "2026-05-11", 1L, 2L)
        );
        EconomicIndicatorMessage message = new EconomicIndicatorMessage(
                "US_CPI_MONTHLY", "CPI", "US", new BigDecimal("3.5"), "2026-05-11", 1L, 2L, "economic-ind");
        given(mapper.toMessage(domain)).willReturn(message);
        EcoIndPublisher sut = new EcoIndPublisher(kafkaTemplate, mapper);

        sut.publish(domain);

        then(kafkaTemplate).should().send(EconomicTopics.INDICATOR, "US_CPI_MONTHLY", message);
    }
}
