package com.example.demo.market_data.application.usecase.publish_price;

import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import com.example.demo.market_data.domain.domain.Premium;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PublishPremiumServiceTest {

    @Mock DomainToMessage<Premium, PremiumMessage> mapper;
    @Mock PublishPriceValuePort<PremiumMessage> port;

    @Test
    @DisplayName("process — mapper.toMessage(premium) 결과를 port.publish에 전달")
    void process_convertsAndPublishes() {
        // given
        PublishPremiumService sut = new PublishPremiumService(mapper, port);
        Premium premium = new Premium("BTC", 10L, 20L,
                new BigDecimal("1.23"), new BigDecimal("1.45"), 8_888L);
        PremiumMessage msg = new PremiumMessage("BTC", 10L, 20L,
                new BigDecimal("1.23"), new BigDecimal("1.45"), 8_888L);
        given(mapper.toMessage(premium)).willReturn(msg);

        // when
        sut.process(premium);

        // then
        then(port).should().publish(msg);
    }
}
