package com.example.demo.market_data.infrastructure.persistence.repo;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.market_data.infrastructure.persistence.entity.PremiumDetailEntity;
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
class PremiumDetailRepositoryTest {

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
    PremiumDetailRepository repository;

    private static PremiumDetailEntity entity(String symbol, long baseId, long compareId) {
        return PremiumDetailEntity.builder()
                .symbol(symbol)
                .baseExchangeId(baseId)
                .compareExchangeId(compareId)
                .baseBid(new BigDecimal("50000000"))
                .baseAsk(new BigDecimal("50100000"))
                .baseQuoteVal(BigDecimal.ONE)
                .compareBid(new BigDecimal("36000"))
                .compareAsk(new BigDecimal("36100"))
                .compareQuoteVal(new BigDecimal("0.000725"))
                .timestamp(6_666L)
                .build();
    }

    @Test
    @DisplayName("saveAll — 저장 후 ID 자동 할당됨")
    void saveAll_assignsIds() {
        // given
        List<PremiumDetailEntity> entities = List.of(
                entity("BTC", 10L, 20L),
                entity("ETH", 10L, 30L)
        );

        // when
        List<PremiumDetailEntity> saved = repository.saveAll(entities);

        // then
        assertThat(saved).allSatisfy(e -> assertThat(e.getId()).isNotNull());
    }

    @Test
    @DisplayName("saveAll — 저장된 엔티티를 findAll로 조회하면 9개 원시 필드 전부 보존")
    void saveAll_thenFindAll_allRawFieldsPreserved() {
        // given
        PremiumDetailEntity e = entity("BTC", 10L, 20L);

        // when
        repository.saveAll(List.of(e));
        List<PremiumDetailEntity> found = repository.findAll();

        // then
        assertThat(found).hasSize(1);
        PremiumDetailEntity stored = found.get(0);
        assertThat(stored.getSymbol()).isEqualTo("BTC");
        assertThat(stored.getBaseExchangeId()).isEqualTo(10L);
        assertThat(stored.getCompareExchangeId()).isEqualTo(20L);
        assertThat(stored.getBaseBid()).isEqualByComparingTo(new BigDecimal("50000000"));
        assertThat(stored.getBaseAsk()).isEqualByComparingTo(new BigDecimal("50100000"));
        assertThat(stored.getBaseQuoteVal()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(stored.getCompareBid()).isEqualByComparingTo(new BigDecimal("36000"));
        assertThat(stored.getCompareAsk()).isEqualByComparingTo(new BigDecimal("36100"));
        assertThat(stored.getCompareQuoteVal()).isEqualByComparingTo(new BigDecimal("0.000725"));
        assertThat(stored.getTimestamp()).isEqualTo(6_666L);
    }
}
