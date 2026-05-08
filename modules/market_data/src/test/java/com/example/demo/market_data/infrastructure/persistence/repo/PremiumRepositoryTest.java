package com.example.demo.market_data.infrastructure.persistence.repo;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.market_data.infrastructure.persistence.entity.PremiumEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
class PremiumRepositoryTest {

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
    PremiumRepository repository;

    private static PremiumEntity entity(String symbol, long baseId, long compareId) {
        return PremiumEntity.builder()
                .symbol(symbol)
                .baseExchangeId(baseId)
                .compareExchangeId(compareId)
                .bid(new BigDecimal("1.23456789"))
                .ask(new BigDecimal("1.98765432"))
                .timestamp(8_888L)
                .build();
    }

    @Test
    @DisplayName("saveAll — 저장 후 ID 자동 할당됨")
    void saveAll_assignsIds() {
        // given
        List<PremiumEntity> entities = List.of(
                entity("BTC", 10L, 20L),
                entity("ETH", 10L, 30L)
        );

        // when
        List<PremiumEntity> saved = repository.saveAll(entities);

        // then
        assertThat(saved).allSatisfy(e -> assertThat(e.getId()).isNotNull());
    }

    @Test
    @DisplayName("saveAll — 저장된 엔티티를 findAll로 조회하면 동일 데이터")
    void saveAll_thenFindAll_returnsStoredEntities() {
        // given
        PremiumEntity e = entity("BTC", 10L, 20L);

        // when
        repository.saveAll(List.of(e));
        List<PremiumEntity> found = repository.findAll();

        // then
        assertThat(found).hasSize(1);
        PremiumEntity stored = found.getFirst();
        assertThat(stored.getSymbol()).isEqualTo("BTC");
        assertThat(stored.getBaseExchangeId()).isEqualTo(10L);
        assertThat(stored.getCompareExchangeId()).isEqualTo(20L);
        assertThat(stored.getBid()).isEqualByComparingTo(new BigDecimal("1.23456789"));
        assertThat(stored.getAsk()).isEqualByComparingTo(new BigDecimal("1.98765432"));
        assertThat(stored.getTimestamp()).isEqualTo(8_888L);
    }
}
