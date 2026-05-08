package com.example.demo.api.composition.service;

import com.example.demo.api.composition.dto.DashboardResponse;
import com.example.demo.economic_query.application.usecase.GetEconomicCalendarUseCase;
import com.example.demo.market_data_query.application.dto.TickBulkView;
import com.example.demo.market_data_query.application.usecase.GetLatestTickBulkUseCase;
import com.example.demo.market_data_query.application.usecase.GetPremiumRankingUseCase;
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
class DashboardServiceTest {

    @Mock GetLatestTickBulkUseCase getLatestTickBulkUseCase;
    @Mock GetPremiumRankingUseCase getPremiumRankingUseCase;
    @Mock GetEconomicCalendarUseCase getEconomicCalendarUseCase;

    @InjectMocks
    DashboardService sut;

    @Test
    @DisplayName("execute — 틱·랭킹·캘린더를 조합해 DashboardResponse 반환")
    void execute_assemblesDashboardResponse() {
        // given
        TickBulkView tick = new TickBulkView(1L, 1_000L, new BigDecimal("50000"), new BigDecimal("50001"));
        given(getLatestTickBulkUseCase.execute(List.of(1L))).willReturn(List.of(tick));
        given(getPremiumRankingUseCase.execute(5)).willReturn(List.of());
        given(getEconomicCalendarUseCase.execute(0L, 86_400_000L)).willReturn(List.of());

        // when
        DashboardResponse result = sut.execute(List.of(1L), 5, 0L, 86_400_000L);

        // then
        assertThat(result.latestTicks()).containsExactly(tick);
        assertThat(result.premiumRanking()).isEmpty();
        assertThat(result.economicCalendar()).isEmpty();
    }

    @Test
    @DisplayName("execute — 빈 marketCodeIds에도 정상 조합")
    void execute_emptyMarketCodeIds_returnsEmptyTicks() {
        // given
        given(getLatestTickBulkUseCase.execute(List.of())).willReturn(List.of());
        given(getPremiumRankingUseCase.execute(10)).willReturn(List.of());
        given(getEconomicCalendarUseCase.execute(0L, 0L)).willReturn(List.of());

        // when
        DashboardResponse result = sut.execute(List.of(), 10, 0L, 0L);

        // then
        assertThat(result.latestTicks()).isEmpty();
    }
}
