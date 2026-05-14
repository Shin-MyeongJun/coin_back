package com.example.demo.user.infrastructure.persistence.adapter;

import com.example.demo.user.application.port.out.LoadApiKeyByPrefixPort;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.infrastructure.persistence.mapper.ApiKeyMapper;
import com.example.demo.user.infrastructure.persistence.repo.ApiKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadApiKeyByPrefixAdapter implements LoadApiKeyByPrefixPort {

    private final ApiKeyJpaRepository repo;
    private final ApiKeyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ApiKey> findByPrefix(ApiKeyPrefix prefix) {
        return repo.findByPrefix(prefix.value()).map(mapper::toDomain);
    }
}
