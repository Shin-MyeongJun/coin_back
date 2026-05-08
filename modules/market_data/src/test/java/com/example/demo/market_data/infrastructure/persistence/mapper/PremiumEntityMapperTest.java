package com.example.demo.market_data.infrastructure.persistence.mapper;

import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumEntityMapperTest {

    PremiumEntityMapper sut = new PremiumEntityMapper();

    private static Premium premium() {
        return new Premium("BTC", 10L, 20L,
                new BigDecimal("1.23456789"), new BigDecimal("1.98765432"), 8_888L);
    }

    @Test
    @DisplayName("toEntity — 모든 도메인 필드 Entity에 매핑")
    void toEntity_mapsAllFields() {
        // given
        Premium premium = premium();

        // when
        PremiumEntity entity = sut.toEntity(premium);

        // then
        assertThat(entity.getSymbol()).isEqualTo("BTC");
        assertThat(entity.getBaseExchangeId()).isEqualTo(10L);
        assertThat(entity.getCompareExchangeId()).isEqualTo(20L);
        assertThat(entity.getBid()).isEqualByComparingTo(premium.bid());
        assertThat(entity.getAsk()).isEqualByComparingTo(premium.ask());
        assertThat(entity.getTimestamp()).isEqualTo(8_888L);
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("toDomain — Entity → Domain 라운드트립 전 필드 보존")
    void toDomain_roundtrip_allFieldsPreserved() {
        // given
        Premium original = premium();

        // when
        Premium restored = sut.toDomain(sut.toEntity(original));

        // then
        assertThat(restored.symbol()).isEqualTo(original.symbol());
        assertThat(restored.baseExchangeId()).isEqualTo(original.baseExchangeId());
        assertThat(restored.compareExchangeId()).isEqualTo(original.compareExchangeId());
        assertThat(restored.bid()).isEqualByComparingTo(original.bid());
        assertThat(restored.ask()).isEqualByComparingTo(original.ask());
        assertThat(restored.timestamp()).isEqualTo(original.timestamp());
    }
}
