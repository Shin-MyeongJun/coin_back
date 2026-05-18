package com.example.demo.alert.application.port.out;

import com.example.demo.alert.application.usecase.AlertFiringCursorPage;

public interface LoadAlertFiringPort {
    AlertFiringCursorPage findByUser(String userId, Long cursor, int limit);
}
