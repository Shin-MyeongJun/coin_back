package com.example.demo.alert.infrastructure.persistence.adapter;

import com.example.demo.alert.application.port.out.LoadAlertFiringPort;
import com.example.demo.alert.application.port.out.SaveAlertFiringPort;
import com.example.demo.alert.application.usecase.AlertFiringCursorPage;
import com.example.demo.alert.domain.domain.AlertFiring;
import com.example.demo.alert.infrastructure.persistence.entity.AlertFiringEntity;
import com.example.demo.alert.infrastructure.persistence.mapper.AlertFiringEntityMapper;
import com.example.demo.alert.infrastructure.persistence.repo.AlertFiringJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertFiringPersistenceAdapter implements LoadAlertFiringPort, SaveAlertFiringPort {
    private final AlertFiringJpaRepository repository;
    private final AlertFiringEntityMapper mapper;

    @Override
    public AlertFiringCursorPage findByUser(String userId, Long cursor, int limit) {
        int normalizedLimit = normalizeLimit(limit);
        PageRequest pageRequest = PageRequest.of(0, normalizedLimit + 1);
        List<AlertFiringEntity> rows = cursor == null
                ? repository.findByUserIdOrderByFiredAtDesc(userId, pageRequest)
                : repository.findByUserIdAndFiredAtLessThanOrderByFiredAtDesc(userId, cursor, pageRequest);
        boolean hasMore = rows.size() > normalizedLimit;
        List<AlertFiring> items = rows.stream()
                .limit(normalizedLimit)
                .map(mapper::toDomain)
                .toList();
        Long nextCursor = hasMore && !items.isEmpty() ? items.get(items.size() - 1).firedAt() : null;
        return new AlertFiringCursorPage(items, nextCursor, hasMore);
    }

    @Override
    public AlertFiring save(AlertFiring firing) {
        return mapper.toDomain(repository.save(mapper.toEntity(firing)));
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return 50;
        }
        return Math.min(limit, 200);
    }
}
