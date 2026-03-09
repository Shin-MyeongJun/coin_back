package com.example.demo.meta_data.application.usecase.Initializer;

import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.meta_data.application.port.out.PublishMetaPort;
import com.example.demo.meta_data.application.usecase.base.InitMetaService;
import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public class InitMarketCodeService extends InitMetaService<MarketCodeEntity, MarketCodeMessage> {
    public InitMarketCodeService(JpaRepository<MarketCodeEntity, Long> repo, DomainToMessage<MarketCodeEntity, MarketCodeMessage> mapper, PublishMetaPort<MarketCodeMessage> port) {
        super(repo, mapper, port);
    }
}
