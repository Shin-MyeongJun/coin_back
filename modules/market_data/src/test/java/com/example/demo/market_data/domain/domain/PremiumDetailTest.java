package com.example.demo.market_data.domain.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumDetailTest {

    /**
     * PremiumDetail.bid() = (compareBid * compareQuoteVal) / (baseAsk * baseQuoteVal) - 1) * 100
     * PremiumDetail.ask() = (compareAsk * compareQuoteVal) / (baseBid * baseQuoteVal) - 1) * 100
     */

    private static PremiumDetail make(
            String baseBid, String baseAsk, String baseQuoteVal,
            String compareBid, String compareAsk, String compareQuoteVal) {
        return new PremiumDetail(
                "BTC", 10L, 20L,
                new BigDecimal(baseBid),
                new BigDecimal(baseAsk),
                new BigDecimal(baseQuoteVal),
                new BigDecimal(compareBid),
                new BigDecimal(compareAsk),
                new BigDecimal(compareQuoteVal),
                1_000L
        );
    }

    @Test
    @DisplayName("bid() = (compareBid * compareQuoteVal) / (baseAsk * baseQuoteVal) - 1) * 100, scale=8 HALF_UP")
    void bid_formula_correct() {
        // given — Upbit(base) vs Binance(compare)
        // baseAsk=50100000 KRW, baseQuoteVal=1
        // compareBid=36000 USD, compareQuoteVal=0.000725 (1 KRW → USD)
        PremiumDetail sut = make("50000000", "50100000", "1",
                "36000", "36100", "0.000725");

        // when
        BigDecimal bid = sut.bid();

        // then
        BigDecimal compare = new BigDecimal("36000").multiply(new BigDecimal("0.000725"));
        BigDecimal base    = new BigDecimal("50100000").multiply(BigDecimal.ONE);
        BigDecimal expected = compare.divide(base, 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        assertThat(bid).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("ask() = (compareAsk * compareQuoteVal) / (baseBid * baseQuoteVal) - 1) * 100, scale=8 HALF_UP")
    void ask_formula_correct() {
        // given
        PremiumDetail sut = make("50000000", "50100000", "1",
                "36000", "36100", "0.000725");

        // when
        BigDecimal ask = sut.ask();

        // then
        BigDecimal compare = new BigDecimal("36100").multiply(new BigDecimal("0.000725"));
        BigDecimal base    = new BigDecimal("50000000").multiply(BigDecimal.ONE);
        BigDecimal expected = compare.divide(base, 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        assertThat(ask).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("baseQuoteVal=1 이고 compareQuoteVal=1 이면 단순 가격 비율로 계산")
    void bid_bothQuoteOne_pureRatio() {
        // given — 같은 통화 기준 (비율 1)
        PremiumDetail sut = make("100", "101", "1", "102", "103", "1");

        // when
        BigDecimal bid = sut.bid();

        // then  bid = (102/101 - 1) * 100
        BigDecimal expected = new BigDecimal("102")
                .divide(new BigDecimal("101"), 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        assertThat(bid).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("compare > base 이면 양수 프리미엄")
    void bid_compareHigherThanBase_positivePremium() {
        // given — compare 가격이 명백히 높음
        PremiumDetail sut = make("100", "101", "1", "200", "201", "1");

        // when & then
        assertThat(sut.bid()).isGreaterThan(BigDecimal.ZERO);
        assertThat(sut.ask()).isGreaterThan(BigDecimal.ZERO);
    }
}
