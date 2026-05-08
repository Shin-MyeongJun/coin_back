package com.example.demo.api.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OffsetPageTest {

    @Test
    @DisplayName("of — 모든 필드를 그대로 설정")
    void of_setsAllFields() {
        // given / when
        OffsetPage<String> page = OffsetPage.of(List.of("x", "y"), 2, 10, 100L);

        // then
        assertThat(page.items()).containsExactly("x", "y");
        assertThat(page.page()).isEqualTo(2);
        assertThat(page.size()).isEqualTo(10);
        assertThat(page.total()).isEqualTo(100L);
    }

    @Test
    @DisplayName("of — 첫 페이지(0) 빈 아이템")
    void of_firstPageEmptyItems() {
        // given / when
        OffsetPage<Integer> page = OffsetPage.of(List.of(), 0, 20, 0L);

        // then
        assertThat(page.items()).isEmpty();
        assertThat(page.page()).isZero();
        assertThat(page.total()).isZero();
    }
}
