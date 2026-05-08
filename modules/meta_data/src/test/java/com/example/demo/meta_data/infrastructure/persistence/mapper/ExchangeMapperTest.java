package com.example.demo.meta_data.infrastructure.persistence.mapper;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.meta_data.domain.ExchangeStatus;
import com.example.demo.meta_data.domain.ExchangeType;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeMapperTest {

    private final ExchangeMapper sut = new ExchangeMapper();

    @Test
    @DisplayName("toDomain — raw 메시지를 ExchangeEntity로 변환, ACTIVE 상태 기본값")
    void toDomain_mapsAllFields() {
        // given
        MarketCodeRawMessage raw = new MarketCodeRawMessage(
                "Upbit", "SPOT", "KR", "KRW", "BTC", "KRW-BTC");

        // when
        ExchangeEntity entity = sut.toDomain(raw);

        // then
        assertThat(entity.getKey().getName()).isEqualTo("Upbit");
        assertThat(entity.getKey().getExchangeType()).isEqualTo(ExchangeType.SPOT);
        assertThat(entity.getKey().getQuote()).isEqualTo("KRW");
        assertThat(entity.getKey().getCountry()).isEqualTo("KR");
        assertThat(entity.getStatus()).isEqualTo(ExchangeStatus.ACTIVE);
    }

    @Test
    @DisplayName("toDomain — 미지원 exchangeType은 OTHER로 fallback")
    void toDomain_unknownType_fallsBackToOther() {
        // given
        MarketCodeRawMessage raw = new MarketCodeRawMessage(
                "Unknown", "INVALID_TYPE", "US", "USD", "ETH", "ETHUSDT");

        // when
        ExchangeEntity entity = sut.toDomain(raw);

        // then
        assertThat(entity.getKey().getExchangeType()).isEqualTo(ExchangeType.OTHER);
    }

    @Test
    @DisplayName("toMessage — ExchangeEntity를 ExchangeMessage로 변환")
    void toMessage_mapsAllFields() {
        // given
        ExchangeEntity entity = ExchangeEntity.builder()
                .id(10L)
                .key(ExchangeKey.builder()
                        .name("Binance")
                        .exchangeType(ExchangeType.PERPETUAL)
                        .quote("USDT")
                        .country("US")
                        .build())
                .status(ExchangeStatus.ACTIVE)
                .build();

        // when
        ExchangeMessage msg = sut.toMessage(entity);

        // then
        assertThat(msg.id()).isEqualTo(10L);
        assertThat(msg.name()).isEqualTo("Binance");
        assertThat(msg.type()).isEqualTo("PERPETUAL");
        assertThat(msg.quote()).isEqualTo("USDT");
    }
}
