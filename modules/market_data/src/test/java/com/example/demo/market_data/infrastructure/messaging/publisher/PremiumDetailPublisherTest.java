package com.example.demo.market_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PremiumDetailPublisherTest {

    @Mock KafkaTemplate<String, PremiumDetailMessage> kafkaTemplate;
    @Captor ArgumentCaptor<String> topicCaptor;
    @Captor ArgumentCaptor<String> keyCaptor;
    @Captor ArgumentCaptor<PremiumDetailMessage> messageCaptor;

    @Test
    @DisplayName("publish — 토픽명이 정확히 'market-data.premium-detail'")
    void publish_topicIsMarketDataPremiumDetail() {
        // given
        PremiumDetailPublisher sut = new PremiumDetailPublisher(kafkaTemplate);
        PremiumDetailMessage msg = new PremiumDetailMessage("BTC", 10L, 20L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                6_666L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        assertThat(topicCaptor.getValue()).isEqualTo("market-data.premium-detail");
    }

    @Test
    @DisplayName("publish — key가 message.extractKey() 반환값과 일치(symbol_baseId_compareId)")
    void publish_keyEqualsExtractKey() {
        // given
        PremiumDetailPublisher sut = new PremiumDetailPublisher(kafkaTemplate);
        PremiumDetailMessage msg = new PremiumDetailMessage("ETH", 10L, 30L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                5_555L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        assertThat(keyCaptor.getValue()).isEqualTo(msg.extractKey());
    }
}
