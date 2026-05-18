package com.example.demo.alert.application.port.in;

import com.example.demo.alert.application.usecase.AlertFiringCursorPage;

public interface QueryAlertFiringUseCase {
    AlertFiringCursorPage findByUser(String userId, Long cursor, int limit);
}
