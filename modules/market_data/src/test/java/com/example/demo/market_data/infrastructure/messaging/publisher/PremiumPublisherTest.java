package com.example.demo.market_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.price_value.PremiumMessage;
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
class PremiumPublisherTest {

    @Mock KafkaTemplate<String, PremiumMessage> kafkaTemplate;
    @Captor ArgumentCaptor<String> topicCaptor;
    @Captor ArgumentCaptor<String> keyCaptor;
    @Captor ArgumentCaptor<PremiumMessage> messageCaptor;

    @Test
    @DisplayName("publish — 토픽명이 정확히 'market-data.premium'")
    void publish_topicIsMarketDataPremium() {
        // given
        PremiumPublisher sut = new PremiumPublisher(kafkaTemplate);
        PremiumMessage msg = new PremiumMessage("BTC", 10L, 20L,
                new BigDecimal("1.23"), new BigDecimal("1.45"), 8_888L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        assertThat(topicCaptor.getValue()).isEqualTo("market-data.premium");
    }

    @Test
    @DisplayName("publish — key가 message.extractKey() 반환값과 일치(symbol_baseId_compareId)")
    void publish_keyEqualsExtractKey() {
        // given
        PremiumPublisher sut = new PremiumPublisher(kafkaTemplate);
        PremiumMessage msg = new PremiumMessage("ETH", 10L, 30L,
                new BigDecimal("0.5"), new BigDecimal("0.6"), 7_777L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        assertThat(keyCaptor.getValue()).isEqualTo(msg.extractKey());
    }
}
