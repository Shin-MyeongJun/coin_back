package com.example.demo.market_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.price_value.TickMessage;
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
class TickPublisherTest {

    @Mock KafkaTemplate<String, TickMessage> kafkaTemplate;
    @Captor ArgumentCaptor<String> topicCaptor;
    @Captor ArgumentCaptor<String> keyCaptor;
    @Captor ArgumentCaptor<TickMessage> messageCaptor;

    @Test
    @DisplayName("publish — 토픽명이 정확히 'market-data.tick'")
    void publish_topicIsMarketDataTick() {
        // given
        TickPublisher sut = new TickPublisher(kafkaTemplate);
        TickMessage msg = new TickMessage(42L, new BigDecimal("50000"), new BigDecimal("50100"), 9_999L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        assertThat(topicCaptor.getValue()).isEqualTo("market-data.tick");
    }

    @Test
    @DisplayName("publish — key가 message.extractKey() 반환값과 일치")
    void publish_keyEqualsExtractKey() {
        // given
        TickPublisher sut = new TickPublisher(kafkaTemplate);
        TickMessage msg = new TickMessage(42L, new BigDecimal("50000"), new BigDecimal("50100"), 9_999L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        assertThat(keyCaptor.getValue()).isEqualTo(msg.extractKey());
    }

    @Test
    @DisplayName("publish — message 인자가 그대로 전달됨")
    void publish_messagePassedThrough() {
        // given
        TickPublisher sut = new TickPublisher(kafkaTemplate);
        TickMessage msg = new TickMessage(7L, new BigDecimal("1000"), new BigDecimal("1001"), 1_000L);

        // when
        sut.publish(msg);

        // then
        then(kafkaTemplate).should().send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isSameAs(msg);
    }
}
