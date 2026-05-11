package com.example.demo.analystics.infrastructure.persistence.repo.candle;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.infrastructure.persistence.entity.candle.TickCandleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisabledInAotMode
class TickCandleRepositoryTest {

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
    TickCandleRepository repository;

    private static TickCandleEntity entity(long marketCodeId, Interval interval) {
        return TickCandleEntity.builder()
                .marketCodeId(marketCodeId)
                .interval(interval)
                .open(new BigDecimal("100.00"))
                .high(new BigDecimal("110.00"))
                .low(new BigDecimal("90.00"))
                .close(new BigDecimal("105.00"))
                .bucketOpenTs(0L)
                .bucketCloseTs(60_000L)
                .observeOpenTs(1_000L)
                .observeCloseTs(59_000L)
                .build();
    }

    @Test
    @DisplayName("saveAll — 저장 후 ID 자동 할당")
    void saveAll_assignsIds() {
        // given
        List<TickCandleEntity> entities = List.of(
                entity(1L, Interval.M1),
                entity(2L, Interval.M5)
        );

        // when
        List<TickCandleEntity> saved = repository.saveAll(entities);

        // then
        assertThat(saved).allSatisfy(e -> assertThat(e.getId()).isNotNull());
    }

    @Test
    @DisplayName("saveAll — findAll로 조회하면 모든 OHLC 필드 보존")
    void saveAll_thenFindAll_allFieldsPreserved() {
        // given
        TickCandleEntity e = entity(42L, Interval.M1);

        // when
        repository.saveAll(List.of(e));
        List<TickCandleEntity> found = repository.findAll();

        // then
        assertThat(found).hasSize(1);
        TickCandleEntity stored = found.get(0);
        assertThat(stored.getMarketCodeId()).isEqualTo(42L);
        assertThat(stored.getInterval()).isEqualTo(Interval.M1);
        assertThat(stored.getOpen()).isEqualByComparingTo("100.00");
        assertThat(stored.getHigh()).isEqualByComparingTo("110.00");
        assertThat(stored.getLow()).isEqualByComparingTo("90.00");
        assertThat(stored.getClose()).isEqualByComparingTo("105.00");
        assertThat(stored.getBucketOpenTs()).isEqualTo(0L);
        assertThat(stored.getBucketCloseTs()).isEqualTo(60_000L);
        assertThat(stored.getObserveOpenTs()).isEqualTo(1_000L);
        assertThat(stored.getObserveCloseTs()).isEqualTo(59_000L);
    }
}
