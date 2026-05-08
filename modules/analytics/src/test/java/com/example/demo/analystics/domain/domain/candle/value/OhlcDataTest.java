package com.example.demo.analystics.domain.domain.candle.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OhlcDataTest {

    @Test
    @DisplayName("생성 시 open/high/low/close 모두 초기값으로 설정")
    void constructor_allFieldsEqualInitialValue() {
        // given / when
        OhlcData<BigDecimal> ohlc = new OhlcData<>(new BigDecimal("100"));

        // then
        assertThat(ohlc.open()).isEqualByComparingTo("100");
        assertThat(ohlc.high()).isEqualByComparingTo("100");
        assertThat(ohlc.low()).isEqualByComparingTo("100");
        assertThat(ohlc.close()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("setHighLow — 더 높은 값이 들어오면 high와 close만 갱신")
    void setHighLow_higherValue_updatesHighAndClose() {
        // given
        OhlcData<BigDecimal> ohlc = new OhlcData<>(new BigDecimal("100"));

        // when
        ohlc.setHighLow(new BigDecimal("150"));

        // then
        assertThat(ohlc.open()).isEqualByComparingTo("100");
        assertThat(ohlc.high()).isEqualByComparingTo("150");
        assertThat(ohlc.low()).isEqualByComparingTo("100");
        assertThat(ohlc.close()).isEqualByComparingTo("150");
    }

    @Test
    @DisplayName("setHighLow — 더 낮은 값이 들어오면 low와 close만 갱신")
    void setHighLow_lowerValue_updatesLowAndClose() {
        // given
        OhlcData<BigDecimal> ohlc = new OhlcData<>(new BigDecimal("100"));

        // when
        ohlc.setHighLow(new BigDecimal("50"));

        // then
        assertThat(ohlc.open()).isEqualByComparingTo("100");
        assertThat(ohlc.high()).isEqualByComparingTo("100");
        assertThat(ohlc.low()).isEqualByComparingTo("50");
        assertThat(ohlc.close()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("setHighLow — 중간값이면 close만 갱신, H/L 불변")
    void setHighLow_middleValue_onlyCloseUpdated() {
        // given
        OhlcData<BigDecimal> ohlc = new OhlcData<>(new BigDecimal("100"));
        ohlc.setHighLow(new BigDecimal("200"));
        ohlc.setHighLow(new BigDecimal("50"));

        // when
        ohlc.setHighLow(new BigDecimal("120"));

        // then
        assertThat(ohlc.open()).isEqualByComparingTo("100");
        assertThat(ohlc.high()).isEqualByComparingTo("200");
        assertThat(ohlc.low()).isEqualByComparingTo("50");
        assertThat(ohlc.close()).isEqualByComparingTo("120");
    }

    @Test
    @DisplayName("4-arg 생성자는 각 필드를 독립적으로 설정")
    void fourArgConstructor_setsFieldsIndependently() {
        // given / when
        OhlcData<BigDecimal> ohlc = new OhlcData<>(
                new BigDecimal("10"), new BigDecimal("20"),
                new BigDecimal("5"),  new BigDecimal("15"));

        // then
        assertThat(ohlc.open()).isEqualByComparingTo("10");
        assertThat(ohlc.high()).isEqualByComparingTo("20");
        assertThat(ohlc.low()).isEqualByComparingTo("5");
        assertThat(ohlc.close()).isEqualByComparingTo("15");
    }
}
