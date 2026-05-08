package com.example.demo.meta_data.application.usecase;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.meta_data.application.port.in.HandleSaveMetaUseCase;
import com.example.demo.meta_data.application.port.out.PublishMetaPort;
import com.example.demo.meta_data.domain.ExchangeStatus;
import com.example.demo.meta_data.domain.ExchangeType;
import com.example.demo.meta_data.infrastructure.persistence.entity.ExchangeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.MarketCodeEntity;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.ExchangeKey;
import com.example.demo.meta_data.infrastructure.persistence.entity.embeddable.MarketCodeKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class ConsumeMetaRawServiceTest {

    @Mock MessageToDomain<MarketCodeRawMessage, ExchangeEntity>   exchangeRawMapper;
    @Mock MessageToDomain<MarketCodeRawMessage, MarketCodeEntity> marketCodeRawMapper;
    @Mock DomainToMessage<ExchangeEntity, ExchangeMessage>        exchangeEntityMapper;
    @Mock DomainToMessage<MarketCodeEntity, MarketCodeMessage>    marketCodeEntityMapper;
    @Mock HandleSaveMetaUseCase<ExchangeEntity>                   exchangeSaveService;
    @Mock HandleSaveMetaUseCase<MarketCodeEntity>                 marketCodeSaveService;
    @Mock PublishMetaPort<ExchangeMessage>                        exchangePublishPort;
    @Mock PublishMetaPort<MarketCodeMessage>                      marketCodePublishPort;

    @Test
    @DisplayName("handle — rawMapper → saveService → publisher 순서로 처리")
    void handle_fullPipelineInOrder() {
        // given
        MarketCodeRawMessage raw = new MarketCodeRawMessage(
                "Upbit", "SPOT", "KR", "KRW", "BTC", "KRW-BTC");

        ExchangeEntity exEntity = ExchangeEntity.builder()
                .key(ExchangeKey.builder().name("Upbit")
                        .exchangeType(ExchangeType.SPOT).quote("KRW").country("KR").build())
                .status(ExchangeStatus.ACTIVE).build();
        ExchangeEntity savedEx = ExchangeEntity.builder().id(1L).key(exEntity.getKey())
                .status(ExchangeStatus.ACTIVE).build();

        MarketCodeEntity mcEntity = MarketCodeEntity.builder()
                .key(MarketCodeKey.builder().exchangeId(1L)
                        .baseAsset("BTC").quoteAsset("KRW").tradingPair("KRW-BTC").build())
                .build();
        MarketCodeEntity savedMc = MarketCodeEntity.builder().id(2L).key(mcEntity.getKey()).build();

        ExchangeMessage exMsg = new ExchangeMessage(1L, "Upbit", "SPOT", "KRW");
        MarketCodeMessage mcMsg = new MarketCodeMessage(2L, 1L, "BTC", "KRW-BTC");

        given(exchangeRawMapper.toDomain(raw)).willReturn(exEntity);
        given(marketCodeRawMapper.toDomain(raw)).willReturn(mcEntity);
        given(exchangeSaveService.handle(exEntity)).willReturn(savedEx);
        given(marketCodeSaveService.handle(any())).willReturn(savedMc);
        given(exchangeEntityMapper.toMessage(savedEx)).willReturn(exMsg);
        given(marketCodeEntityMapper.toMessage(savedMc)).willReturn(mcMsg);

        ConsumeMetaRawService sut = new ConsumeMetaRawService(
                exchangeRawMapper, marketCodeRawMapper,
                exchangeEntityMapper, marketCodeEntityMapper,
                exchangeSaveService, marketCodeSaveService,
                exchangePublishPort, marketCodePublishPort);

        // when
        sut.handle(raw);

        // then
        InOrder order = inOrder(exchangeSaveService, marketCodeSaveService,
                exchangePublishPort, marketCodePublishPort);
        order.verify(exchangeSaveService).handle(exEntity);
        order.verify(marketCodeSaveService).handle(any());
        order.verify(exchangePublishPort).publish(exMsg);
        order.verify(marketCodePublishPort).publish(mcMsg);
    }

    @Test
    @DisplayName("handle — exchangeId가 savedExchange.getId()로 설정됨")
    void handle_setsExchangeIdFromSavedExchange() {
        // given
        MarketCodeRawMessage raw = new MarketCodeRawMessage(
                "Binance", "SPOT", "US", "USDT", "ETH", "ETHUSDT");

        ExchangeEntity exEntity = ExchangeEntity.builder()
                .key(ExchangeKey.builder().name("Binance")
                        .exchangeType(ExchangeType.SPOT).quote("USDT").country("US").build())
                .status(ExchangeStatus.ACTIVE).build();
        ExchangeEntity savedEx = ExchangeEntity.builder().id(42L).key(exEntity.getKey())
                .status(ExchangeStatus.ACTIVE).build();

        MarketCodeKey mcKey = MarketCodeKey.builder()
                .exchangeId(1L) // 초기값 (overwritten by handle)
                .baseAsset("ETH").quoteAsset("USDT").tradingPair("ETHUSDT").build();
        MarketCodeEntity mcEntity = MarketCodeEntity.builder().key(mcKey).build();
        MarketCodeEntity savedMc = MarketCodeEntity.builder().id(5L).key(mcKey).build();

        given(exchangeRawMapper.toDomain(raw)).willReturn(exEntity);
        given(marketCodeRawMapper.toDomain(raw)).willReturn(mcEntity);
        given(exchangeSaveService.handle(exEntity)).willReturn(savedEx);
        given(marketCodeSaveService.handle(any())).willReturn(savedMc);
        given(exchangeEntityMapper.toMessage(savedEx)).willReturn(new ExchangeMessage(42L, "Binance", "SPOT", "USDT"));
        given(marketCodeEntityMapper.toMessage(savedMc)).willReturn(new MarketCodeMessage(5L, 42L, "ETH", "ETHUSDT"));

        ConsumeMetaRawService sut = new ConsumeMetaRawService(
                exchangeRawMapper, marketCodeRawMapper,
                exchangeEntityMapper, marketCodeEntityMapper,
                exchangeSaveService, marketCodeSaveService,
                exchangePublishPort, marketCodePublishPort);

        // when
        sut.handle(raw);

        // then — exchangeId was set to savedEx.getId() = 42
        then(marketCodeSaveService).should().handle(
                org.mockito.ArgumentMatchers.argThat(mc ->
                        mc.getKey().getExchangeId().equals(42L)));
    }
}
