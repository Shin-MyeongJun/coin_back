package com.example.demo.meta_data.infrastructure.persistence.repo;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵합니다.

import com.example.demo.meta_data.domain.ExchangeStatus;
import com.example.demo.meta_data.domain.ExchangeType;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
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
class ExchangeRepositoryTest {

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
    ExchangeRepository repository;

    private static ExchangeEntity entity(String name, ExchangeType type, String quote, String country) {
        return ExchangeEntity.builder()
                .key(ExchangeKey.builder()
                        .name(name).exchangeType(type).quote(quote).country(country).build())
                .status(ExchangeStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("save — ID 자동 할당")
    void save_assignsId() {
        // when
        ExchangeEntity saved = repository.save(entity("Upbit", ExchangeType.SPOT, "KRW", "KR"));

        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("findByKey — 저장한 후 동일 key로 조회 가능")
    void findByKey_existingKey_returnsEntity() {
        // given
        ExchangeKey key = ExchangeKey.builder()
                .name("Binance").exchangeType(ExchangeType.SPOT).quote("USDT").country("US").build();
        repository.save(ExchangeEntity.builder().key(key).status(ExchangeStatus.ACTIVE).build());

        // when
        Optional<ExchangeEntity> result = repository.findByKey(key);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getKey().getName()).isEqualTo("Binance");
    }

    @Test
    @DisplayName("findByKey — 없는 key이면 Optional.empty()")
    void findByKey_nonExistent_returnsEmpty() {
        // given
        ExchangeKey key = ExchangeKey.builder()
                .name("Missing").exchangeType(ExchangeType.OTHER).quote("XXX").country("ZZ").build();

        // when
        Optional<ExchangeEntity> result = repository.findByKey(key);

        // then
        assertThat(result).isEmpty();
    }
}
