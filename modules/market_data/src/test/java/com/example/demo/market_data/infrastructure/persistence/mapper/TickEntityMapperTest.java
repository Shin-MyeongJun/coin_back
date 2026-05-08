package com.example.demo.market_data.infrastructure.persistence.mapper;

import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.infrastructure.persistence.entity.TickEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TickEntityMapperTest {

    TickEntityMapper sut = new TickEntityMapper();

    private static Tick tick() {
        return new Tick(42L, new BigDecimal("50000.12345678"), new BigDecimal("50100.98765432"), 9_999L);
    }

    @Test
    @DisplayName("toEntity — 모든 도메인 필드 Entity에 매핑")
    void toEntity_mapsAllFields() {
        // given
        Tick tick = tick();

        // when
        TickEntity entity = sut.toEntity(tick);

        // then
        assertThat(entity.getMarketCodeId()).isEqualTo(42L);
        assertThat(entity.getBid()).isEqualByComparingTo(tick.bid());
        assertThat(entity.getAsk()).isEqualByComparingTo(tick.ask());
        assertThat(entity.getTimestamp()).isEqualTo(9_999L);
        // id는 DB가 생성하므로 null
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("toDomain — Entity → Domain 라운드트립 전 필드 보존")
    void toDomain_roundtrip_allFieldsPreserved() {
        // given
        Tick original = tick();

        // when
        Tick restored = sut.toDomain(sut.toEntity(original));

        // then
        assertThat(restored.marketCodeId()).isEqualTo(original.marketCodeId());
        assertThat(restored.bid()).isEqualByComparingTo(original.bid());
        assertThat(restored.ask()).isEqualByComparingTo(original.ask());
        assertThat(restored.timestamp()).isEqualTo(original.timestamp());
    }
}
