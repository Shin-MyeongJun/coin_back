package com.example.demo.market_data.domain.service;

import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.domain.domain.PriceValue;
import com.example.demo.market_data.domain.domain.Tick;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceValueConverterTest {

    PriceValueConverter sut = new PriceValueConverter();

    @Test
    @DisplayName("convert(Tick) — bid, ask, timestamp 필드 매핑 정확")
    void convert_tick_mapsAllFields() {
        // given
        Tick tick = new Tick(1L, new BigDecimal("50000"), new BigDecimal("50100"), 9_999L);

        // when
        PriceValue result = sut.convert(tick);

        // then
        assertThat(result.bid()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(result.ask()).isEqualByComparingTo(new BigDecimal("50100"));
        assertThat(result.timestamp()).isEqualTo(9_999L);
    }

    @Test
    @DisplayName("convert(Premium) — bid(), ask(), timestamp 위임")
    void convert_premium_mapsAllFields() {
        // given
        Premium premium = new Premium("BTC", 10L, 20L,
                new BigDecimal("1.23"), new BigDecimal("1.45"), 8_888L);

        // when
        PriceValue result = sut.convert(premium);

        // then
        assertThat(result.bid()).isEqualByComparingTo(new BigDecimal("1.23"));
        assertThat(result.ask()).isEqualByComparingTo(new BigDecimal("1.45"));
        assertThat(result.timestamp()).isEqualTo(8_888L);
    }

    @Test
    @DisplayName("convert(PremiumDetail) — bid()/ask() 계산값 및 timestamp 매핑")
    void convert_premiumDetail_mapsComputedBidAsk() {
        // given — PremiumDetail.bid()/ask()는 내부 계산 로직 존재
        PremiumDetail pd = new PremiumDetail("BTC", 10L, 20L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                7_777L);

        // when
        PriceValue result = sut.convert(pd);

        // then — 계산 결과는 PremiumDetail.bid()/ask() 에 위임, 동치만 확인
        assertThat(result.bid()).isEqualByComparingTo(pd.bid());
        assertThat(result.ask()).isEqualByComparingTo(pd.ask());
        assertThat(result.timestamp()).isEqualTo(7_777L);
    }
}
