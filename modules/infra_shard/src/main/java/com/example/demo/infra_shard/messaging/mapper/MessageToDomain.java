package com.example.demo.infra_shard.messaging.mapper;

public interface MessageToDomain<MESSAGE,DOMAIN> {
    DOMAIN toDomain(MESSAGE message);
}
