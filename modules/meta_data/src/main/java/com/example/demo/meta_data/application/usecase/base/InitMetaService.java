package com.example.demo.meta_data.application.usecase.base;

import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.meta_data.application.port.in.InitializeMetaUseCase;
import com.example.demo.meta_data.application.port.out.PublishMetaPort;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class InitMetaService<ENTITY,MESSAGE> implements InitializeMetaUseCase {

    private final JpaRepository<ENTITY,Long> repo;
    private final DomainToMessage<ENTITY,MESSAGE> mapper;
    private final PublishMetaPort<MESSAGE> port;

    public InitMetaService(JpaRepository<ENTITY, Long> repo, DomainToMessage<ENTITY, MESSAGE> mapper, PublishMetaPort<MESSAGE> port) {
        this.repo = repo;
        this.mapper = mapper;
        this.port = port;
    }

    @Override
    public void initSend() {
        repo.findAll().stream().map(mapper::toMessage).forEach(port::publish);
    }
}
