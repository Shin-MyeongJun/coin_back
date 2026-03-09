package com.example.demo.infra_shard.messaging.mapper;

public interface DomainToMessage<DOMAIN,MESSAGE> {
    MESSAGE toMessage(DOMAIN domain);
}
