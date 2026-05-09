package com.example.demo.market_data.domain.buffer;

import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.domain.service.PremiumKeyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumDetailBufferTest {

    PremiumDetailBuffer sut;

    @BeforeEach
    void setUp() {
        sut = new PremiumDetailBuffer(new PremiumKeyParser());
    }

    private static PremiumDetail detail(String symbol, Long baseId, Long compareId) {
        return new PremiumDetail(
                symbol, baseId, compareId,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                1_000L
        );
    }

    @Test
    @DisplayName("add 후 flush — 추가한 항목 전부 반환")
    void add_then_flush_returnsAllItems() {
        // given
        PremiumDetail d1 = detail("BTC", 10L, 20L);
        PremiumDetail d2 = detail("ETH", 10L, 30L);
        sut.add(d1);
        sut.add(d2);

        // when
        List<PremiumDetail> result = sut.flush();

        // then
        assertThat(result).hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(d1, d2);
    }

    @Test
    @DisplayName("동일 PremiumKey 재add — 최신값으로 덮어쓴다")
    void add_sameKey_overwritesWithLatest() {
        // given
        PremiumDetail old    = new PremiumDetail("BTC", 10L, 20L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"), new BigDecimal("36100"), new BigDecimal("0.000725"), 1_000L);
        PremiumDetail latest = new PremiumDetail("BTC", 10L, 20L,
                new BigDecimal("51000000"), new BigDecimal("51100000"), BigDecimal.ONE,
                new BigDecimal("37000"), new BigDecimal("37100"), new BigDecimal("0.000725"), 2_000L);
        sut.add(old);
        sut.add(latest);

        // when
        List<PremiumDetail> result = sut.flush();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().baseBid()).isEqualByComparingTo(new BigDecimal("51000000"));
    }

    @Test
    @Disabled("COIN-BUG-001 수정 후 더 이상 유효하지 않음 — 다음 PR 에서 삭제 예정")
    @DisplayName("현재 동작 — flush 후 재flush도 동일 데이터 반환(clear 없음)")
    void flush_twice_returnsSameData() {
        // given
        sut.add(detail("BTC", 10L, 20L));

        // when
        List<PremiumDetail> first  = sut.flush();
        List<PremiumDetail> second = sut.flush();

        // then
        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
    }
}
