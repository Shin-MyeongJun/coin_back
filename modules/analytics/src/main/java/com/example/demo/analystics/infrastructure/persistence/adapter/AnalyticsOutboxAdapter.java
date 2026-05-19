package com.example.demo.analystics.infrastructure.persistence.adapter;

import com.example.demo.analystics.application.port.out.LoadPendingOutboxPort;
import com.example.demo.analystics.application.port.out.MarkOutboxPublishedPort;
import com.example.demo.analystics.application.port.out.SaveOutboxRecordPort;
import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import com.example.demo.analystics.infrastructure.persistence.entity.AnalyticsOutboxEntity;
import com.example.demo.analystics.infrastructure.persistence.mapper.AnalyticsOutboxEntityMapper;
import com.example.demo.analystics.infrastructure.persistence.repo.AnalyticsOutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalyticsOutboxAdapter implements SaveOutboxRecordPort, LoadPendingOutboxPort, MarkOutboxPublishedPort {

    private final AnalyticsOutboxJpaRepository repo;
    private final AnalyticsOutboxEntityMapper mapper;

    @Override
    public void save(AnalyticsOutboxRecord record) {
        repo.save(mapper.toEntity(record));
    }

    @Override
    public List<AnalyticsOutboxRecord> loadPending(int batchSize, int maxRetry) {
        Pageable page = PageRequest.of(0, batchSize);
        List<AnalyticsOutboxEntity> entities = repo.findPending(maxRetry, page);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public void markPublished(Long id, long publishedAt) {
        repo.markPublished(id, publishedAt);
    }

    @Override
    public void incrementRetry(Long id) {
        repo.incrementRetry(id);
    }
}
