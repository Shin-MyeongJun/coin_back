package com.example.demo.alert.application.usecase;

import com.example.demo.alert.domain.domain.AlertFiring;

import java.util.List;

public record AlertFiringCursorPage(
        List<AlertFiring> items,
        Long nextCursor,
        boolean hasMore
) {
}
