package com.example.demo.user.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityMapping;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper implements EntityMapping<Account, AccountEntity> {

    @Override
    public AccountEntity toEntity(Account account) {
        return AccountEntity.builder()
                .id(account.getId().value())
                .email(account.getEmail().value())
                .passwordHash(account.getPasswordHash().value())
                .tier(account.getTier())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    public Account toDomain(AccountEntity entity) {
        return new Account(
                AccountId.of(entity.getId()),
                new Email(entity.getEmail()),
                PasswordHash.of(entity.getPasswordHash()),
                entity.getTier(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
