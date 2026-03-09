package com.example.demo.infra_shard.messaging.mapper;

public interface MessageMapping<DOMAIN,MESSAGE>
        extends DomainToMessage<DOMAIN,MESSAGE>
        ,MessageToDomain<MESSAGE,DOMAIN>  {
}
