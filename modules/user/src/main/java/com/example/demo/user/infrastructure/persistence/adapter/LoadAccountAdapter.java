package com.example.demo.user.infrastructure.persistence.adapter;

import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.infrastructure.persistence.mapper.AccountMapper;
import com.example.demo.user.infrastructure.persistence.repo.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadAccountAdapter implements LoadAccountPort {

    private final AccountJpaRepository repo;
    private final AccountMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findById(AccountId id) {
        return repo.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(Email email) {
        return repo.findByEmail(email.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Email email) {
        return repo.existsByEmail(email.value());
    }
}
