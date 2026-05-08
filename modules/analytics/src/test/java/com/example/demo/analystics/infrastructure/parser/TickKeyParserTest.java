package com.example.demo.analystics.infrastructure.parser;

import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.contracts.message.price_value.TickMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TickKeyParserTest {

    private final TickKeyParser sut = new TickKeyParser();

    @Test
    @DisplayName("parseKey — marketCodeId를 TickKey로 변환")
    void parseKey_returnsTickKeyWithMarketCodeId() {
        // given
        TickMessage msg = new TickMessage(42L, new BigDecimal("50000"), new BigDecimal("50100"), 1_000L);

        // when
        TickKey key = sut.parseKey(msg);

        // then
        assertThat(key.MarketCodeId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("parseValue — bid 값 반환")
    void parseValue_returnsBid() {
        // given
        TickMessage msg = new TickMessage(1L, new BigDecimal("12345.678"), new BigDecimal("12346.000"), 999L);

        // when
        BigDecimal value = sut.parseValue(msg);

        // then
        assertThat(value).isEqualByComparingTo("12345.678");
    }
}
