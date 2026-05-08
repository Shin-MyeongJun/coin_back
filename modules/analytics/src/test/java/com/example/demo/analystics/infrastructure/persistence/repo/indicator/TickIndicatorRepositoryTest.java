package com.example.demo.analystics.infrastructure.persistence.repo.indicator;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.infrastructure.persistence.entity.indicator.TickIndicatorEntity;
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
class TickIndicatorRepositoryTest {

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
    TickIndicatorRepository repository;

    private static TickIndicatorEntity entity(long marketCodeId, TradeIndicatorType type, int period) {
        return TickIndicatorEntity.builder()
                .marketCodeId(marketCodeId)
                .interval(Interval.M1)
                .type(type)
                .period(period)
                .value(new BigDecimal("55.12345678"))
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
        List<TickIndicatorEntity> entities = List.of(
                entity(1L, TradeIndicatorType.EMA, 12),
                entity(1L, TradeIndicatorType.RSI, 14)
        );

        // when
        List<TickIndicatorEntity> saved = repository.saveAll(entities);

        // then
        assertThat(saved).allSatisfy(e -> assertThat(e.getId()).isNotNull());
    }

    @Test
    @DisplayName("saveAll — findAll로 조회하면 모든 필드 보존")
    void saveAll_thenFindAll_allFieldsPreserved() {
        // given
        TickIndicatorEntity e = entity(99L, TradeIndicatorType.EMA, 26);

        // when
        repository.saveAll(List.of(e));
        List<TickIndicatorEntity> found = repository.findAll();

        // then
        assertThat(found).hasSize(1);
        TickIndicatorEntity stored = found.get(0);
        assertThat(stored.getMarketCodeId()).isEqualTo(99L);
        assertThat(stored.getInterval()).isEqualTo(Interval.M1);
        assertThat(stored.getType()).isEqualTo(TradeIndicatorType.EMA);
        assertThat(stored.getPeriod()).isEqualTo(26);
        assertThat(stored.getValue()).isEqualByComparingTo("55.12345678");
        assertThat(stored.getBucketOpenTs()).isEqualTo(0L);
        assertThat(stored.getBucketCloseTs()).isEqualTo(60_000L);
    }
}
