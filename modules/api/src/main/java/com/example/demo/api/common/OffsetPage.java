package com.example.demo.api.common;

import java.util.List;

public record OffsetPage<T>(
        List<T> items,
        int page,
        int size,
        long total
) {
    public static <T> OffsetPage<T> of(List<T> items, int page, int size, long total) {
        return new OffsetPage<>(items, page, size, total);
    }
}
