package com.example.demo.meta_data.application.usecase.base;

import com.example.demo.meta_data.application.port.out.FindAndSavePort;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import com.example.demo.meta_data.application.usecase.save.SaveExchangeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SaveMetaServiceTest {

    @Mock
    FindAndSavePort<ExchangeEntity, ExchangeKey> repo;

    SaveExchangeService sut;

    private ExchangeKey key = ExchangeKey.builder()
            .name("Upbit").exchangeType(com.example.demo.meta_data.domain.ExchangeType.SPOT)
            .quote("KRW").country("KR").build();

    private ExchangeEntity entity() {
        return ExchangeEntity.builder().key(key).build();
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        sut = new SaveExchangeService(repo);
    }

    @Test
    @DisplayName("handle — key가 이미 존재하면 DB 저장 없이 기존 entity 반환")
    void handle_existingKey_returnsExistingEntity() {
        // given
        ExchangeEntity existing = ExchangeEntity.builder().id(10L).key(key).build();
        given(repo.findByKey(key)).willReturn(Optional.of(existing));

        // when
        ExchangeEntity result = sut.handle(entity());

        // then
        assertThat(result.getId()).isEqualTo(10L);
        then(repo).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("handle — key가 없으면 repo.save 호출 후 반환")
    void handle_newKey_savesAndReturns() {
        // given
        ExchangeEntity saved = ExchangeEntity.builder().id(99L).key(key).build();
        given(repo.findByKey(key)).willReturn(Optional.empty());
        given(repo.save(org.mockito.ArgumentMatchers.any())).willReturn(saved);

        // when
        ExchangeEntity result = sut.handle(entity());

        // then
        assertThat(result.getId()).isEqualTo(99L);
        then(repo).should().save(org.mockito.ArgumentMatchers.any());
    }
}
