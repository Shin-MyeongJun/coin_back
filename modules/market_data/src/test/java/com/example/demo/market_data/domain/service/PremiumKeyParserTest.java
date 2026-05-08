package com.example.demo.market_data.domain.service;

import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.domain.domain.PremiumKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumKeyParserTest {

    PremiumKeyParser sut = new PremiumKeyParser();

    @Test
    @DisplayName("parse(Premium) — symbol, baseExchangeId, compareExchangeId 매핑")
    void parse_premium_createsCorrectKey() {
        // given
        Premium premium = new Premium("BTC", 10L, 20L,
                new BigDecimal("1.5"), new BigDecimal("1.6"), 1_000L);

        // when
        PremiumKey key = sut.parse(premium);

        // then
        assertThat(key).usingRecursiveComparison().isEqualTo(
                new PremiumKey("BTC", 10L, 20L));
    }

    @Test
    @DisplayName("parse(PremiumDetail) — symbol, baseExchangeId, compareExchangeId 매핑")
    void parse_premiumDetail_createsCorrectKey() {
        // given
        PremiumDetail pd = new PremiumDetail("ETH", 10L, 30L,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1_000L);

        // when
        PremiumKey key = sut.parse(pd);

        // then
        assertThat(key).usingRecursiveComparison().isEqualTo(
                new PremiumKey("ETH", 10L, 30L));
    }
}
