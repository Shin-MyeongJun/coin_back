package com.example.demo.market_data.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.market_data.domain.domain.Tick;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TickMessageMapperTest {

    TickMessageMapper sut = new TickMessageMapper();

    private static Tick tick() {
        return new Tick(42L, new BigDecimal("50000.12345678"), new BigDecimal("50100.98765432"), 9_999L);
    }

    @Test
    @DisplayName("toMessage — 모든 필드 매핑 정확")
    void toMessage_mapsAllFields() {
        // given
        Tick tick = tick();

        // when
        TickMessage msg = sut.toMessage(tick);

        // then
        assertThat(msg.marketCodeId()).isEqualTo(42L);
        assertThat(msg.bid()).isEqualByComparingTo(tick.bid());
        assertThat(msg.ask()).isEqualByComparingTo(tick.ask());
        assertThat(msg.timestamp()).isEqualTo(9_999L);
    }

    @Test
    @DisplayName("toDomain — 모든 필드 매핑 정확 (라운드트립)")
    void toDomain_roundtrip_allFieldsPreserved() {
        // given
        Tick original = tick();

        // when
        Tick restored = sut.toDomain(sut.toMessage(original));

        // then
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
