package com.example.demo.analystics.infrastructure.messaging.mapper;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import com.example.demo.analystics.domain.domain.candle.close.PremiumCloseCandle;
import com.example.demo.analystics.domain.domain.candle.close.PremiumDetailCloseCandle;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.close.PremiumCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.infrastructure.messaging.mapper.candle.PremiumCandleMessageMapper;
import com.example.demo.analystics.infrastructure.messaging.mapper.candle.PremiumDetailCandleMapper;
import com.example.demo.analystics.infrastructure.messaging.mapper.candle.TickCandleMessageMapper;
import com.example.demo.analystics.infrastructure.messaging.mapper.indicator.PremiumIndicatorMessageMapper;
import com.example.demo.analystics.infrastructure.messaging.mapper.indicator.TickIndicatorMessageMapper;
import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsMessageMapperTest {

    private static final TimeWindow TIMES = new TimeWindow(
            1710000000000L,
            1710000059999L,
            1710000000100L,
            1710000059900L
    );

    @Test
    @DisplayName("TickCloseCandle maps to TickCandleMessage string payload")
    void tickCandle_toMessage() {
        TickCloseCandle domain = new TickCloseCandle(
                1L,
                Interval.M1,
                new BigDecimal("100.00"),
                new BigDecimal("110.00"),
                new BigDecimal("95.00"),
                new BigDecimal("105.00"),
                TIMES
        );

        TickCandleMessage message = new TickCandleMessageMapper().toMessage(domain);

        assertThat(message).isEqualTo(new TickCandleMessage(
                "1",
                "1m",
                "100.00",
                "110.00",
                "95.00",
                "105.00",
                "1710000000000",
                "1710000059999",
                "1710000000100",
                "1710000059900"
        ));
    }

    @Test
    @DisplayName("PremiumCloseCandle maps to PremiumCandleMessage string payload")
    void premiumCandle_toMessage() {
        PremiumCloseCandle domain = new PremiumCloseCandle(
                "BTC",
                1L,
                2L,
                Interval.M3,
                new BigDecimal("1.10"),
                new BigDecimal("1.30"),
                new BigDecimal("1.00"),
                new BigDecimal("1.25"),
                TIMES
        );

        PremiumCandleMessage message = new PremiumCandleMessageMapper().toMessage(domain);

        assertThat(message).isEqualTo(new PremiumCandleMessage(
                "BTC",
                "1",
                "2",
                "3m",
                "1.10",
                "1.30",
                "1.00",
                "1.25",
                "1710000000000",
                "1710000059999",
                "1710000000100",
                "1710000059900"
        ));
    }

    @Test
    @DisplayName("PremiumDetailCloseCandle maps nested values to PremiumDetailCandleMessage")
    void premiumDetailCandle_toMessage() {
        PremiumDetailCloseCandle domain = new PremiumDetailCloseCandle(
                "BTC",
                1L,
                2L,
                Interval.M5,
                detail("10", "100", "20", "200"),
                detail("11", "110", "22", "220"),
                detail("9", "90", "18", "180"),
                detail("10.5", "105", "21", "210"),
                TIMES
        );

        PremiumDetailCandleMessage message = new PremiumDetailCandleMapper().toMessage(domain);

        assertThat(message).isEqualTo(new PremiumDetailCandleMessage(
                "BTC",
                "1",
                "2",
                "5m",
                "10",
                "100",
                "20",
                "200",
                "11",
                "110",
                "22",
                "220",
                "9",
                "90",
                "18",
                "180",
                "10.5",
                "105",
                "21",
                "210",
                "1710000000000",
                "1710000059999",
                "1710000000100",
                "1710000059900"
        ));
    }

    @Test
    @DisplayName("TickCloseIndicator maps to TickIndicatorMessage string payload")
    void tickIndicator_toMessage() {
        TickCloseIndicator domain = new TickCloseIndicator(
                1L,
                Interval.M15,
                TradeIndicatorType.EMA,
                14,
                new BigDecimal("105.25"),
                TIMES
        );

        TickIndicatorMessage message = new TickIndicatorMessageMapper().toMessage(domain);

        assertThat(message).isEqualTo(new TickIndicatorMessage(
                "1",
                "15m",
                "EMA",
                "14",
                "105.25",
                "1710000000000",
                "1710000059999",
                "1710000000100",
                "1710000059900"
        ));
    }

    @Test
    @DisplayName("PremiumCloseIndicator maps to PremiumIndicatorMessage string payload")
    void premiumIndicator_toMessage() {
        PremiumCloseIndicator domain = new PremiumCloseIndicator(
                "BTC",
                1L,
                2L,
                Interval.M30,
                TradeIndicatorType.RSI,
                14,
                new BigDecimal("48.15"),
                TIMES
        );

        PremiumIndicatorMessage message = new PremiumIndicatorMessageMapper().toMessage(domain);

        assertThat(message).isEqualTo(new PremiumIndicatorMessage(
                "BTC",
                "1",
                "2",
                "30m",
                "RSI",
                "14",
                "48.15",
                "1710000000000",
                "1710000059999",
                "1710000000100",
                "1710000059900"
        ));
    }

    private static PremiumDetailValue detail(String baseVal, String baseQuoteVal, String compareVal, String compareQuoteVal) {
        return new PremiumDetailValue(
                new BigDecimal(baseVal),
                new BigDecimal(baseQuoteVal),
                new BigDecimal(compareVal),
                new BigDecimal(compareQuoteVal)
        );
    }
}
