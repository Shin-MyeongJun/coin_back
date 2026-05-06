package com.example.demo.api.common;

import java.util.List;

public record CursorPage<T>(
        List<T> items,
        Long nextCursor,
        boolean hasMore
) {
    public static <T> CursorPage<T> of(List<T> items, Long nextCursor) {
        return new CursorPage<>(items, nextCursor, nextCursor != null);
    }

    public static <T> CursorPage<T> last(List<T> items) {
        return new CursorPage<>(items, null, false);
    }
}
