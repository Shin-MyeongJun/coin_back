package com.example.demo.market_data.application.usecase.publish_price;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PublishPremiumDetailServiceTest {

    @Mock DomainToMessage<PremiumDetail, PremiumDetailMessage> mapper;
    @Mock PublishPriceValuePort<PremiumDetailMessage> port;

    @Test
    @DisplayName("process — mapper.toMessage(detail) 결과를 port.publish에 전달")
    void process_convertsAndPublishes() {
        // given
        PublishPremiumDetailService sut = new PublishPremiumDetailService(mapper, port);
        PremiumDetail detail = new PremiumDetail("BTC", 10L, 20L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                6_666L);
        PremiumDetailMessage msg = new PremiumDetailMessage("BTC", 10L, 20L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                6_666L);
        given(mapper.toMessage(detail)).willReturn(msg);

        // when
        sut.process(detail);

        // then
        then(port).should().publish(msg);
    }
}
