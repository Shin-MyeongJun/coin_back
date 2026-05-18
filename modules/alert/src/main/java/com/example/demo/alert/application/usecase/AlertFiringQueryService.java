package com.example.demo.alert.application.usecase;

import com.example.demo.alert.application.port.in.QueryAlertFiringUseCase;
import com.example.demo.alert.application.port.out.LoadAlertFiringPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertFiringQueryService implements QueryAlertFiringUseCase {
    private final LoadAlertFiringPort loadAlertFiringPort;

    @Override
    @Transactional(readOnly = true)
    public AlertFiringCursorPage findByUser(String userId, Long cursor, int limit) {
        return loadAlertFiringPort.findByUser(userId, cursor, limit);
    }
}
