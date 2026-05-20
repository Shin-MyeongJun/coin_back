package com.example.demo.analystics.infrastructure.persistence.adapter;

import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import com.example.demo.analystics.infrastructure.persistence.entity.AnalyticsOutboxEntity;
import com.example.demo.analystics.infrastructure.persistence.mapper.AnalyticsOutboxEntityMapper;
import com.example.demo.analystics.infrastructure.persistence.repo.AnalyticsOutboxJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AnalyticsOutboxAdapterTest {

    @Mock
    AnalyticsOutboxJpaRepository repo;

    AnalyticsOutboxEntityMapper mapper;

    AnalyticsOutboxAdapter sut;

    @BeforeEach
    void setUp() {
        mapper = new AnalyticsOutboxEntityMapper();
        sut = new AnalyticsOutboxAdapter(repo, mapper);
    }

    @Test
    @DisplayName("save — domain record를 entity로 매핑해 repo.save 호출")
    void save_mapsDomainToEntityAndSaves() {
        // given
        AnalyticsOutboxRecord record = AnalyticsOutboxRecord.pending(
                "TICK_CANDLE", "agg-1", "analytics.tick-candle", "{\"k\":1}", 100L);

        // when
        sut.save(record);

        // then
        ArgumentCaptor<AnalyticsOutboxEntity> captor = ArgumentCaptor.forClass(AnalyticsOutboxEntity.class);
        then(repo).should().save(captor.capture());
        AnalyticsOutboxEntity saved = captor.getValue();
        assertThat(saved.getAggregateType()).isEqualTo("TICK_CANDLE");
        assertThat(saved.getAggregateId()).isEqualTo("agg-1");
        assertThat(saved.getTopic()).isEqualTo("analytics.tick-candle");
        assertThat(saved.getPayloadJson()).isEqualTo("{\"k\":1}");
        assertThat(saved.getCreatedAt()).isEqualTo(100L);
        assertThat(saved.getPublishedAt()).isNull();
        assertThat(saved.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("loadPending — repo.findPending 결과를 domain 리스트로 매핑")
    void loadPending_returnsMappedDomainList() {
        // given
        AnalyticsOutboxEntity entity = AnalyticsOutboxEntity.builder()
                .id(11L)
                .aggregateType("TICK_CANDLE")
                .aggregateId("k")
                .topic("analytics.tick-candle")
                .payloadJson("{}")
                .createdAt(1L)
                .publishedAt(null)
                .retryCount(2)
                .build();
        given(repo.findPendingForUpdateSkipLocked(eq(10), eq(50))).willReturn(List.of(entity));

        // when
        List<AnalyticsOutboxRecord> result = sut.loadPending(50, 10);

        // then
        then(repo).should().findPendingForUpdateSkipLocked(10, 50);
        assertThat(result).hasSize(1);
        AnalyticsOutboxRecord r = result.get(0);
        assertThat(r.id()).isEqualTo(11L);
        assertThat(r.aggregateType()).isEqualTo("TICK_CANDLE");
        assertThat(r.retryCount()).isEqualTo(2);
        assertThat(r.publishedAt()).isNull();
    }

    @Test
    @DisplayName("markPublished — repo.markPublished에 id, publishedAt 전달")
    void markPublished_delegatesToRepo() {
        // when
        sut.markPublished(42L, 999L);

        // then
        then(repo).should().markPublished(42L, 999L);
    }

    @Test
    @DisplayName("incrementRetry — repo.incrementRetry에 id 전달")
    void incrementRetry_delegatesToRepo() {
        // when
        sut.incrementRetry(42L);

        // then
        then(repo).should().incrementRetry(42L);
    }
}
