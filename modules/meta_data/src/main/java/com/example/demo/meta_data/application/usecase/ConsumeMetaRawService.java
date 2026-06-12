package com.example.demo.meta_data.application.usecase;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.meta_data.application.port.in.ConsumeMetaRawUseCase;
import com.example.demo.meta_data.application.port.in.HandleSaveMetaUseCase;
import com.example.demo.meta_data.application.port.out.PublishMetaPort;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumeMetaRawService implements ConsumeMetaRawUseCase {

    private final MessageToDomain<MarketCodeRawMessage, ExchangeEntity> exchangeRawMapper;
    private final MessageToDomain<MarketCodeRawMessage, MarketCodeEntity> marketCodeRawMapper;

    private final DomainToMessage<ExchangeEntity, ExchangeMessage> exchangeEntityMapper;
    private final DomainToMessage<MarketCodeEntity, MarketCodeMessage> marketCodeEntityMapper;

    private final HandleSaveMetaUseCase<ExchangeEntity> exchangeSaveService;
    private final HandleSaveMetaUseCase<MarketCodeEntity> marketCodeSaveService;

    private final PublishMetaPort<ExchangeMessage> exchangePublishPort;
    private final PublishMetaPort<MarketCodeMessage> marketCodePublishPort;


    @Override
    public void handle(MarketCodeRawMessage raw) {
        ExchangeEntity en =  exchangeRawMapper.toDomain(raw);
        MarketCodeEntity mn = marketCodeRawMapper.toDomain(raw);
        log.debug("market code raw mapped");

        ExchangeEntity enm =  exchangeSaveService.handle(en);
        mn.getKey().setExchangeId(enm.getId());// 방안 생각하기
        MarketCodeEntity mnm = marketCodeSaveService.handle(mn);
        log.debug("market code metadata saved");

        exchangePublishPort.publish(exchangeEntityMapper.toMessage(enm));
        marketCodePublishPort.publish(marketCodeEntityMapper.toMessage(mnm));
        log.debug("market code metadata published");

    }
}
