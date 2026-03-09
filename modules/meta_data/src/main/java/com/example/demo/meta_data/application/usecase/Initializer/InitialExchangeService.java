package com.example.demo.meta_data.application.usecase.Initializer;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.meta_data.application.port.out.PublishMetaPort;
import com.example.demo.meta_data.application.usecase.base.InitMetaService;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class InitialExchangeService extends InitMetaService<ExchangeEntity, ExchangeMessage> {
    public InitialExchangeService(JpaRepository<ExchangeEntity, Long> repo, DomainToMessage<ExchangeEntity, ExchangeMessage> mapper, PublishMetaPort<ExchangeMessage> port) {
        super(repo, mapper, port);
    }
}
