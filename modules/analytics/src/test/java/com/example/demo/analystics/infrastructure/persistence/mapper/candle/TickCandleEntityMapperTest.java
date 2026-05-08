package com.example.demo.analystics.infrastructure.persistence.mapper.candle;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import com.example.demo.analystics.domain.domain.candle.close.TickCloseCandle;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.TickCandleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TickCandleEntityMapperTest {

    private final TickCandleEntityMapper sut = new TickCandleEntityMapper();

    @Test
    @DisplayName("toEntity — TickCloseCandle의 모든 필드가 entity에 매핑")
    void toEntity_allFieldsMapped() {
        // given
        TimeWindow tw = new TimeWindow(1_000L, 61_000L, 1_200L, 60_800L);
        TickCloseCandle domain = new TickCloseCandle(
                42L,
                Interval.M1,
                new BigDecimal("100.00"),
                new BigDecimal("110.00"),
                new BigDecimal("90.00"),
                new BigDecimal("105.00"),
                tw
        );

        // when
        TickCandleEntity entity = sut.toEntity(domain);

        // then
        assertThat(entity.getMarketCodeId()).isEqualTo(42L);
        assertThat(entity.getInterval()).isEqualTo(Interval.M1);
        assertThat(entity.getOpen()).isEqualByComparingTo("100.00");
        assertThat(entity.getHigh()).isEqualByComparingTo("110.00");
        assertThat(entity.getLow()).isEqualByComparingTo("90.00");
        assertThat(entity.getClose()).isEqualByComparingTo("105.00");
        assertThat(entity.getBucketOpenTs()).isEqualTo(1_000L);
        assertThat(entity.getBucketCloseTs()).isEqualTo(61_000L);
        assertThat(entity.getObserveOpenTs()).isEqualTo(1_200L);
        assertThat(entity.getObserveCloseTs()).isEqualTo(60_800L);
    }
}
