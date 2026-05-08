package com.example.demo.meta_data.infrastructure.persistence.repo;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisabledInAotMode
class MarketCodeRepositoryTest {

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
    MarketCodeRepository repository;

    private static MarketCodeEntity entity(Long exchangeId, String base, String quote, String pair) {
        return MarketCodeEntity.builder()
                .key(MarketCodeKey.builder()
                        .exchangeId(exchangeId)
                        .baseAsset(base).quoteAsset(quote).tradingPair(pair)
                        .build())
                .build();
    }

    @Test
    @DisplayName("save — ID 자동 할당")
    void save_assignsId() {
        // when
        MarketCodeEntity saved = repository.save(entity(1L, "BTC", "KRW", "KRW-BTC"));

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("findByKey — 저장한 후 동일 key로 조회 가능")
    void findByKey_existingKey_returnsEntity() {
        // given
        MarketCodeKey key = MarketCodeKey.builder()
                .exchangeId(1L).baseAsset("ETH").quoteAsset("USDT").tradingPair("ETHUSDT").build();
        repository.save(MarketCodeEntity.builder().key(key).build());

        // when
        Optional<MarketCodeEntity> result = repository.findByKey(key);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getKey().getBaseAsset()).isEqualTo("ETH");
    }

    @Test
    @DisplayName("findByKey — 없는 key이면 Optional.empty()")
    void findByKey_nonExistent_returnsEmpty() {
        // given
        MarketCodeKey key = MarketCodeKey.builder()
                .exchangeId(999L).baseAsset("XYZ").quoteAsset("ABC").tradingPair("XYZABC").build();

        // when
        Optional<MarketCodeEntity> result = repository.findByKey(key);

        // then
        assertThat(result).isEmpty();
    }
}
