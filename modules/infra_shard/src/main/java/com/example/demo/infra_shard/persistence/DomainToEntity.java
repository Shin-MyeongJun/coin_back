package com.example.demo.infra_shard.persistence;

public interface DomainToEntity<DOMAIN,ENTITY> {
    ENTITY toEntity(DOMAIN domain);
}
