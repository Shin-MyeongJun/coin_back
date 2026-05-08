package com.example.demo.api.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CursorPageTest {

    @Test
    @DisplayName("of — nextCursor 있으면 hasMore=true")
    void of_withNextCursor_hasMoreTrue() {
        // given / when
        CursorPage<String> page = CursorPage.of(List.of("a", "b"), 10L);

        // then
        assertThat(page.items()).containsExactly("a", "b");
        assertThat(page.nextCursor()).isEqualTo(10L);
        assertThat(page.hasMore()).isTrue();
    }

    @Test
    @DisplayName("of — nextCursor=null 이면 hasMore=false")
    void of_withNullNextCursor_hasMoreFalse() {
        // given / when
        CursorPage<String> page = CursorPage.of(List.of("a"), null);

        // then
        assertThat(page.nextCursor()).isNull();
        assertThat(page.hasMore()).isFalse();
    }

    @Test
    @DisplayName("last — hasMore=false, nextCursor=null")
    void last_hasMoreFalseAndNullCursor() {
        // given / when
        CursorPage<Integer> page = CursorPage.last(List.of(1, 2));

        // then
        assertThat(page.hasMore()).isFalse();
        assertThat(page.nextCursor()).isNull();
        assertThat(page.items()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("last — 빈 리스트도 정상 처리")
    void last_emptyList_hasMoreFalse() {
        // given / when
        CursorPage<Object> page = CursorPage.last(List.of());

        // then
        assertThat(page.items()).isEmpty();
        assertThat(page.hasMore()).isFalse();
    }
}
