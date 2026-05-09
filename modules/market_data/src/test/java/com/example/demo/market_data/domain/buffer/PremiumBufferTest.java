package com.example.demo.market_data.domain.buffer;

import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.service.PremiumKeyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumBufferTest {

    PremiumBuffer sut;

    @BeforeEach
    void setUp() {
        // PremiumKeyParser는 순수 로직이므로 실제 객체 주입
        sut = new PremiumBuffer(new PremiumKeyParser());
    }

    private static Premium premium(String symbol, Long baseId, Long compareId, String bid, String ask) {
        return new Premium(symbol, baseId, compareId,
                new BigDecimal(bid), new BigDecimal(ask), 1_000L);
    }

    @Test
    @DisplayName("add 후 flush — 추가한 항목 전부 반환")
    void add_then_flush_returnsAllItems() {
        // given
        Premium p1 = premium("BTC", 10L, 20L, "1.5", "1.6");
        Premium p2 = premium("ETH", 10L, 20L, "0.5", "0.6");
        sut.add(p1);
        sut.add(p2);

        // when
        List<Premium> result = sut.flush();

        // then
        assertThat(result).hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(p1, p2);
    }

    @Test
    @DisplayName("동일 PremiumKey(symbol+baseId+compareId) 재add — 최신값으로 덮어쓴다")
    void add_sameKey_overwritesWithLatest() {
        // given
        Premium old    = premium("BTC", 10L, 20L, "1.0", "1.1");
        Premium latest = premium("BTC", 10L, 20L, "2.0", "2.1");
        sut.add(old);
        sut.add(latest);

        // when
        List<Premium> result = sut.flush();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().bid()).isEqualByComparingTo(new BigDecimal("2.0"));
    }

    @Test
    @Disabled("COIN-BUG-001 수정 후 더 이상 유효하지 않음 — 다음 PR 에서 삭제 예정")
    @DisplayName("현재 동작 — flush 후 재flush도 동일 데이터 반환(clear 없음)")
    void flush_twice_returnsSameData() {
        // given
        sut.add(premium("BTC", 10L, 20L, "1.5", "1.6"));

        // when
        List<Premium> first  = sut.flush();
        List<Premium> second = sut.flush();

        // then
        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
    }
}
