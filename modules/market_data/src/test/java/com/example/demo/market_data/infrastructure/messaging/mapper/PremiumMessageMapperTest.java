package com.example.demo.market_data.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.market_data.domain.domain.Premium;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumMessageMapperTest {

    PremiumMessageMapper sut = new PremiumMessageMapper();

    private static Premium premium() {
        return new Premium("BTC", 10L, 20L,
                new BigDecimal("1.23456789"), new BigDecimal("1.98765432"), 8_888L);
    }

    @Test
    @DisplayName("toMessage — 모든 필드 매핑 정확")
    void toMessage_mapsAllFields() {
        // given
        Premium premium = premium();

        // when
        PremiumMessage msg = sut.toMessage(premium);

        // then
        assertThat(msg.symbol()).isEqualTo("BTC");
        assertThat(msg.baseExchangeId()).isEqualTo(10L);
        assertThat(msg.compareExchangeId()).isEqualTo(20L);
        assertThat(msg.bid()).isEqualByComparingTo(premium.bid());
        assertThat(msg.ask()).isEqualByComparingTo(premium.ask());
        assertThat(msg.timestamp()).isEqualTo(8_888L);
    }

    @Test
    @DisplayName("toDomain — 라운드트립 전 필드 보존")
    void toDomain_roundtrip_allFieldsPreserved() {
        // given
        Premium original = premium();

        // when
        Premium restored = sut.toDomain(sut.toMessage(original));

        // then
        assertThat(restored).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(original);
    }
}
