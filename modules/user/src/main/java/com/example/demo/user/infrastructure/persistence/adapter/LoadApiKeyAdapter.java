package com.example.demo.user.infrastructure.persistence.adapter;

import com.example.demo.user.application.port.out.LoadApiKeyPort;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.infrastructure.persistence.mapper.ApiKeyMapper;
import com.example.demo.user.infrastructure.persistence.repo.ApiKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadApiKeyAdapter implements LoadApiKeyPort {

    private final ApiKeyJpaRepository repo;
    private final ApiKeyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ApiKey> findById(ApiKeyId id) {
        return repo.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKey> findByAccountId(AccountId accountId) {
        return repo.findAllByAccountIdOrderByCreatedAtDesc(accountId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByAccountId(AccountId accountId) {
        return repo.countByAccountIdAndRevokedAtIsNull(accountId.value());
    }
}
