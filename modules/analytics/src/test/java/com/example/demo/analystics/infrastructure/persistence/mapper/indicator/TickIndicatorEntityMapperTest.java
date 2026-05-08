package com.example.demo.analystics.infrastructure.persistence.mapper.indicator;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.close.TickCloseIndicator;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.TickIndicatorEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TickIndicatorEntityMapperTest {

    private final TickIndicatorEntityMapper sut = new TickIndicatorEntityMapper();

    @Test
    @DisplayName("toEntity — TickCloseIndicator의 모든 필드가 entity에 매핑")
    void toEntity_allFieldsMapped() {
        // given
        TimeWindow tw = new TimeWindow(1_000L, 61_000L, 1_100L, 60_900L);
        TickCloseIndicator domain = new TickCloseIndicator(
                99L,
                Interval.M5,
                TradeIndicatorType.EMA,
                12,
                new BigDecimal("55555.123456"),
                tw
        );

        // when
        TickIndicatorEntity entity = sut.toEntity(domain);

        // then
        assertThat(entity.getMarketCodeId()).isEqualTo(99L);
        assertThat(entity.getInterval()).isEqualTo(Interval.M5);
        assertThat(entity.getType()).isEqualTo(TradeIndicatorType.EMA);
        assertThat(entity.getPeriod()).isEqualTo(12);
        assertThat(entity.getValue()).isEqualByComparingTo("55555.123456");
        assertThat(entity.getBucketOpenTs()).isEqualTo(1_000L);
        assertThat(entity.getBucketCloseTs()).isEqualTo(61_000L);
        assertThat(entity.getObserveOpenTs()).isEqualTo(1_100L);
        assertThat(entity.getObserveCloseTs()).isEqualTo(60_900L);
    }
}
