package com.example.demo.meta_data.infrastructure.persistence.mapper;

import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketCodeMapperTest {

    private final MarketCodeMapper sut = new MarketCodeMapper();

    @Test
    @DisplayName("toDomain — raw 메시지를 MarketCodeEntity로 변환")
    void toDomain_mapsAllFields() {
        // given
        MarketCodeRawMessage raw = new MarketCodeRawMessage(
                "Upbit", "SPOT", "KR", "KRW", "BTC", "KRW-BTC");

        // when
        MarketCodeEntity entity = sut.toDomain(raw);

        // then
        assertThat(entity.getKey().getBaseAsset()).isEqualTo("BTC");
        assertThat(entity.getKey().getQuoteAsset()).isEqualTo("KRW");
        assertThat(entity.getKey().getTradingPair()).isEqualTo("KRW-BTC");
    }

    @Test
    @DisplayName("toMessage — MarketCodeEntity를 MarketCodeMessage로 변환")
    void toMessage_mapsAllFields() {
        // given
        MarketCodeEntity entity = MarketCodeEntity.builder()
                .id(5L)
                .key(MarketCodeKey.builder()
                        .exchangeId(1L)
                        .baseAsset("ETH")
                        .quoteAsset("KRW")
                        .tradingPair("KRW-ETH")
                        .build())
                .build();

        // when
        MarketCodeMessage msg = sut.toMessage(entity);

        // then
        assertThat(msg.id()).isEqualTo(5L);
        assertThat(msg.exchangeId()).isEqualTo(1L);
        assertThat(msg.base()).isEqualTo("ETH");
        assertThat(msg.tradingPair()).isEqualTo("KRW-ETH");
    }
}
