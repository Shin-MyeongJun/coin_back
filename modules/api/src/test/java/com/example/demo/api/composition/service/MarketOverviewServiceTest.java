package com.example.demo.api.composition.service;

import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.usecase.GetLatestIndicatorMultiMarketUseCase;
import com.example.demo.api.composition.dto.MarketOverviewResponse;
import com.example.demo.market_data_query.application.dto.PremiumSnapshotView;
import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.market_data_query.application.usecase.GetLatestTickUseCase;
import com.example.demo.market_data_query.application.usecase.GetPremiumSnapshotByBaseUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MarketOverviewServiceTest {

    @Mock GetLatestTickUseCase getLatestTickUseCase;
    @Mock GetPremiumSnapshotByBaseUseCase getPremiumSnapshotByBaseUseCase;
    @Mock GetLatestIndicatorMultiMarketUseCase getLatestIndicatorMultiMarketUseCase;

    @InjectMocks
    MarketOverviewService sut;

    @Test
    @DisplayName("execute — 틱·프리미엄·지표를 조합해 MarketOverviewResponse 반환")
    void execute_allUseCasesReturn_assemblesResponse() {
        // given
        TickLatestView tick = new TickLatestView(1L, 100L, 1000L, new BigDecimal("50000"), new BigDecimal("50001"));
        PremiumSnapshotView snapshot = new PremiumSnapshotView("BTC", 1L, 2L, 1000L, new BigDecimal("0.01"), new BigDecimal("0.02"));
        TickIndicatorView indicator = new TickIndicatorView(100L, "1m", "RSI", 14, new BigDecimal("55.5"), 900L, 1000L, 901L, 999L);

        given(getLatestTickUseCase.execute(100L)).willReturn(Optional.of(tick));
        given(getPremiumSnapshotByBaseUseCase.execute("BTC")).willReturn(List.of(snapshot));
        given(getLatestIndicatorMultiMarketUseCase.execute(List.of(100L), "1m", "RSI")).willReturn(List.of(indicator));

        // when
        MarketOverviewResponse result = sut.execute(100L, "BTC", List.of(100L), "1m", "RSI");

        // then
        assertThat(result.latestTick()).isEqualTo(tick);
        assertThat(result.premiumSnapshot()).containsExactly(snapshot);
        assertThat(result.latestIndicators()).containsExactly(indicator);
    }

    @Test
    @DisplayName("execute — 틱이 없으면 NoSuchElementException, marketCodeId 메시지 포함")
    void execute_tickNotFound_throwsNoSuchElementException() {
        // given
        given(getLatestTickUseCase.execute(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sut.execute(999L, "BTC", List.of(999L), "1m", "RSI"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("execute — 프리미엄·지표 빈 목록이어도 정상 반환")
    void execute_emptyPremiumAndIndicators_returnsEmptyLists() {
        // given
        TickLatestView tick = new TickLatestView(1L, 1L, 0L, BigDecimal.ONE, BigDecimal.TEN);
        given(getLatestTickUseCase.execute(1L)).willReturn(Optional.of(tick));
        given(getPremiumSnapshotByBaseUseCase.execute("ETH")).willReturn(List.of());
        given(getLatestIndicatorMultiMarketUseCase.execute(List.of(1L), "5m", "EMA")).willReturn(List.of());

        // when
        MarketOverviewResponse result = sut.execute(1L, "ETH", List.of(1L), "5m", "EMA");

        // then
        assertThat(result.premiumSnapshot()).isEmpty();
        assertThat(result.latestIndicators()).isEmpty();
    }
}
