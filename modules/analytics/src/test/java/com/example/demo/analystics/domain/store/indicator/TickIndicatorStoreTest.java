package com.example.demo.analystics.domain.store.indicator;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.IndicatorKey;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.domain.factory.indicator.value.TickIndicatorFactory;
import com.example.demo.analystics.domain.service.ClosingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TickIndicatorStoreTest {

    @Mock
    ClosingData<TickIndicator, TickCloseIndicator> closer;

    TickIndicatorStore sut;
    TickKey key1 = new TickKey(1L);

    // TickIndicatorFactory는 8개 indicator key를 등록함 (MEAN, STDDEV, TR, EMA×3, RSI×2)
    TickIndicatorFactory factory = new TickIndicatorFactory();

    @BeforeEach
    void setUp() {
        sut = new TickIndicatorStore(factory, closer);
    }

    @Test
    @DisplayName("update — 첫 update 시 factory.createIndicators(key, val)로 8개 indicator 생성")
    void update_newKey_creates8Indicators() {
        // given / when
        sut.update(key1, new BigDecimal("100"));

        // then
        for (Interval interval : Interval.analyticsSupported()) {
            assertThat(sut.getIndicators(interval)).hasSize(8);
        }
    }

    @Test
    @DisplayName("update — 두 번째 호출은 기존 indicator를 update")
    void update_existingKey_updatesIndicators() {
        // given
        sut.update(key1, new BigDecimal("100"));

        // when
        sut.update(key1, new BigDecimal("200"));

        // then — indicator 수는 그대로 8개
        assertThat(sut.getIndicators(Interval.M1)).hasSize(8);
    }

    @Test
    @DisplayName("drain — closer.toClose 결과를 반환, 이후 open() 호출로 재개방")
    void drain_returnsClosedIndicators() {
        // given
        sut.update(key1, new BigDecimal("100"));
        sut.update(key1, new BigDecimal("200")); // need prevSub for EmaUpdater.close()

        TickCloseIndicator fakeClose = new TickCloseIndicator(
                1L, Interval.M1, null, 0, BigDecimal.ZERO, null);
        given(closer.toClose(any(TickIndicator.class), eq(Interval.M1))).willReturn(fakeClose);

        // when
        List<TickCloseIndicator> result = sut.drain(Interval.M1);

        // then
        assertThat(result).hasSize(8);
        assertThat(result).allMatch(ci -> ci == fakeClose);
    }

    @Test
    @DisplayName("drain — 빈 store이면 빈 리스트")
    void drain_empty_returnsEmptyList() {
        // when
        List<TickCloseIndicator> result = sut.drain(Interval.M1);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("assign — snapshot으로 indicator buffer를 교체")
    void assign_replacesExistingBuffer() {
        // given
        sut.update(key1, new BigDecimal("100")); // 기존

        TickKey key2 = new TickKey(99L);
        TickIndicator restored = factory.createIndicators(key2, new BigDecimal("50"))
                .values().iterator().next();
        Map<Interval, List<TickIndicator>> snapshot = Map.of(Interval.M1, List.of(restored));

        // when
        sut.assign(snapshot);

        // then
        List<TickIndicator> indicators = sut.getIndicators(Interval.M1);
        assertThat(indicators).hasSize(1);
        assertThat(indicators.get(0).getDataKey()).isEqualTo(key2);
    }

    @Test
    @DisplayName("getIndicators — 미지원 interval이면 빈 리스트")
    void getIndicators_unsupportedInterval_returnsEmpty() {
        // given
        sut.update(key1, new BigDecimal("100"));

        // when
        List<TickIndicator> result = sut.getIndicators(Interval.D1);

        // then
        assertThat(result).isEmpty();
    }
}
