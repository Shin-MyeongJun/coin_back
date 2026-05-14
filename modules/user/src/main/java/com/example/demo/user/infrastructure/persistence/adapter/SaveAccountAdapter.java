package com.example.demo.user.infrastructure.persistence.adapter;

import com.example.demo.user.application.port.out.SaveAccountPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.infrastructure.persistence.entity.AccountEntity;
import com.example.demo.user.infrastructure.persistence.mapper.AccountMapper;
import com.example.demo.user.infrastructure.persistence.repo.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SaveAccountAdapter implements SaveAccountPort {

    private final AccountJpaRepository repo;
    private final AccountMapper mapper;

    @Override
    @Transactional
    public Account save(Account account) {
        AccountEntity saved = repo.save(mapper.toEntity(account));
        return mapper.toDomain(saved);
    }
}
