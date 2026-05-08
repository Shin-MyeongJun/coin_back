package com.example.demo.market_data.infrastructure.persistence.repo;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.market_data.infrastructure.persistence.entity.TickEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
class TickRepositoryTest {

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
    TickRepository repository;

    private static TickEntity entity(long marketCodeId, String bid, String ask) {
        return TickEntity.builder()
                .marketCodeId(marketCodeId)
                .bid(new BigDecimal(bid))
                .ask(new BigDecimal(ask))
                .timestamp(9_999L)
                .build();
    }

    @Test
    @DisplayName("saveAll — 저장 후 ID 자동 할당됨")
    void saveAll_assignsIds() {
        // given
        List<TickEntity> entities = List.of(
                entity(1L, "50000.12345678", "50100.98765432"),
                entity(2L, "36000", "36100")
        );

        // when
        List<TickEntity> saved = repository.saveAll(entities);

        // then
        assertThat(saved).allSatisfy(e -> assertThat(e.getId()).isNotNull());
    }

    @Test
    @DisplayName("saveAll — 저장된 엔티티를 findAll로 조회하면 동일 데이터")
    void saveAll_thenFindAll_returnsStoredEntities() {
        // given
        TickEntity e = entity(42L, "50000.12345678", "50100.98765432");

        // when
        repository.saveAll(List.of(e));
        List<TickEntity> found = repository.findAll();

        // then
        assertThat(found).hasSize(1);
        TickEntity stored = found.get(0);
        assertThat(stored.getMarketCodeId()).isEqualTo(42L);
        assertThat(stored.getBid()).isEqualByComparingTo(new BigDecimal("50000.12345678"));
        assertThat(stored.getAsk()).isEqualByComparingTo(new BigDecimal("50100.98765432"));
        assertThat(stored.getTimestamp()).isEqualTo(9_999L);
    }
}
