package com.example.demo.infra_shard.persistence;

public interface EntityToDomain<ENTITY,DOMAIN> {
    DOMAIN toDomain(ENTITY entity);
}
