package com.example.demo.user.infrastructure.persistence.adapter;

import com.example.demo.user.application.port.out.SaveApiKeyPort;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.infrastructure.persistence.entity.ApiKeyEntity;
import com.example.demo.user.infrastructure.persistence.mapper.ApiKeyMapper;
import com.example.demo.user.infrastructure.persistence.repo.ApiKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SaveApiKeyAdapter implements SaveApiKeyPort {

    private final ApiKeyJpaRepository repo;
    private final ApiKeyMapper mapper;

    @Override
    @Transactional
    public ApiKey save(ApiKey apiKey) {
        ApiKeyEntity saved = repo.save(mapper.toEntity(apiKey));
        return mapper.toDomain(saved);
    }
}
