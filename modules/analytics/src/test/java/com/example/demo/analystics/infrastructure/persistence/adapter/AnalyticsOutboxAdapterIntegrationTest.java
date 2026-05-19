package com.example.demo.analystics.infrastructure.persistence.adapter;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.analystics.domain.domain.AnalyticsOutboxRecord;
import com.example.demo.analystics.infrastructure.persistence.entity.AnalyticsOutboxEntity;
import com.example.demo.analystics.infrastructure.persistence.mapper.AnalyticsOutboxEntityMapper;
import com.example.demo.analystics.infrastructure.persistence.repo.AnalyticsOutboxJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisabledInAotMode
@Import({AnalyticsOutboxAdapter.class, AnalyticsOutboxEntityMapper.class})
class AnalyticsOutboxAdapterIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    AnalyticsOutboxAdapter adapter;

    @Autowired
    AnalyticsOutboxJpaRepository repo;

    @Autowired
    TransactionTemplate txTemplate;

    @BeforeEach
    void cleanup() {
        repo.deleteAllInBatch();
    }

    private static AnalyticsOutboxRecord pending(String aggregateId) {
        return AnalyticsOutboxRecord.pending(
                "TICK_CANDLE",
                aggregateId,
                "analytics.tick-candle",
                "{\"id\":\"" + aggregateId + "\"}",
                System.currentTimeMillis());
    }

    @Test
    @DisplayName("save → loadPending → markPublished round-trip")
    void save_thenLoadPending_thenMarkPublished() {
        // when
        adapter.save(pending("a-1"));
        adapter.save(pending("a-2"));

        // then — both visible as pending
        List<AnalyticsOutboxRecord> pending = adapter.loadPending(10, 5);
        assertThat(pending).hasSize(2);

        // mark first as published
        Long firstId = pending.get(0).id();
        adapter.markPublished(firstId, 12345L);

        // only second remains pending
        List<AnalyticsOutboxRecord> still = adapter.loadPending(10, 5);
        assertThat(still).hasSize(1);
        assertThat(still.get(0).id()).isNotEqualTo(firstId);

        AnalyticsOutboxEntity published = repo.findById(firstId).orElseThrow();
        assertThat(published.getPublishedAt()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("incrementRetry — retry_count 증가, max-retry 이상은 loadPending에서 제외")
    void incrementRetry_excludesAfterMaxRetry() {
        // given
        adapter.save(pending("retry-1"));
        Long id = repo.findAll().get(0).getId();

        // when
        adapter.incrementRetry(id);
        adapter.incrementRetry(id);

        // then
        assertThat(repo.findById(id).orElseThrow().getRetryCount()).isEqualTo(2);
        assertThat(adapter.loadPending(10, 3)).hasSize(1); // 2 < 3
        assertThat(adapter.loadPending(10, 2)).isEmpty();   // 2 < 2 = false
    }

    @Test
    @DisplayName("@Transactional 내부 save 다건은 commit 후 모두 조회되며, 롤백 시 모두 사라진다")
    void transactional_savesCommitTogether_andRollbackTogether() {
        // commit case
        txTemplate.executeWithoutResult(status -> {
            adapter.save(pending("tx-1"));
            adapter.save(pending("tx-2"));
        });
        assertThat(repo.findAll()).hasSize(2);

        // rollback case
        try {
            txTemplate.executeWithoutResult(status -> {
                adapter.save(pending("tx-3"));
                adapter.save(pending("tx-4"));
                throw new IllegalStateException("force rollback");
            });
        } catch (IllegalStateException ignored) {
        }
        assertThat(repo.findAll()).hasSize(2); // still only tx-1, tx-2
    }
}
