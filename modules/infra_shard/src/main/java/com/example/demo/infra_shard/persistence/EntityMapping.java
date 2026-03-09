package com.example.demo.infra_shard.persistence;

public interface EntityMapping<DOMAIN,ENTITY>
        extends DomainToEntity<DOMAIN,ENTITY>,
                EntityToDomain<ENTITY,DOMAIN>{
}
