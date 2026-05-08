package com.example.demo.market_data.application.usecase.publish_price;

import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import com.example.demo.market_data.domain.domain.Tick;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PublishTickServiceTest {

    @Mock DomainToMessage<Tick, TickMessage> mapper;
    @Mock PublishPriceValuePort<TickMessage> port;

    @Test
    @DisplayName("process — mapper.toMessage(tick) 결과를 port.publish에 전달")
    void process_convertsAndPublishes() {
        // given
        PublishTickService sut = new PublishTickService(mapper, port);
        Tick tick = new Tick(1L, new BigDecimal("50000"), new BigDecimal("50100"), 9_999L);
        TickMessage msg = new TickMessage(1L, new BigDecimal("50000"), new BigDecimal("50100"), 9_999L);
        given(mapper.toMessage(tick)).willReturn(msg);

        // when
        sut.process(tick);

        // then
        then(port).should().publish(msg);
    }
}
