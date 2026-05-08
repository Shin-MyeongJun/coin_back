package com.example.demo.api.composition.service;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.usecase.GetTickCandleSeriesUseCase;
import com.example.demo.analytics_query.application.usecase.GetTickIndicatorSeriesUseCase;
import com.example.demo.api.composition.dto.ChartResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ChartCompositionServiceTest {

    @Mock GetTickCandleSeriesUseCase getTickCandleSeriesUseCase;
    @Mock GetTickIndicatorSeriesUseCase getTickIndicatorSeriesUseCase;

    @InjectMocks
    ChartCompositionService sut;

    @Test
    @DisplayName("execute — 캔들·지표 시리즈를 조합해 ChartResponse 반환")
    void execute_assemblesChartResponse() {
        // given
        TickCandleView candle = new TickCandleView(1L, "1m",
                new BigDecimal("100"), new BigDecimal("110"),
                new BigDecimal("95"), new BigDecimal("108"),
                0L, 60_000L, 1L, 59_999L);
        TickIndicatorView indicator = new TickIndicatorView(
                1L, "1m", "RSI", 14, new BigDecimal("55"),
                0L, 60_000L, 1L, 59_999L);

        given(getTickCandleSeriesUseCase.execute(1L, "1m", 0L, 60_000L)).willReturn(List.of(candle));
        given(getTickIndicatorSeriesUseCase.execute(1L, "1m", "RSI", 0L, 60_000L)).willReturn(List.of(indicator));

        // when
        ChartResponse result = sut.execute(1L, "1m", "RSI", 0L, 60_000L);

        // then
        assertThat(result.candles()).containsExactly(candle);
        assertThat(result.indicators()).containsExactly(indicator);
    }

    @Test
    @DisplayName("execute — 빈 시리즈도 ChartResponse로 정상 반환")
    void execute_emptySeriesData_returnsEmptyResponse() {
        // given
        given(getTickCandleSeriesUseCase.execute(2L, "5m", 0L, 300_000L)).willReturn(List.of());
        given(getTickIndicatorSeriesUseCase.execute(2L, "5m", "EMA", 0L, 300_000L)).willReturn(List.of());

        // when
        ChartResponse result = sut.execute(2L, "5m", "EMA", 0L, 300_000L);

        // then
        assertThat(result.candles()).isEmpty();
        assertThat(result.indicators()).isEmpty();
    }
}
