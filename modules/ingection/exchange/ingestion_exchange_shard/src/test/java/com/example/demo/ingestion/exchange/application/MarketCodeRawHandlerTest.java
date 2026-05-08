package com.example.demo.ingestion.exchange.application;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.ingestion.exchange.application.port.in.GetMarketCodeRaw;
import com.example.demo.ingestion.exchange.application.port.in.ParseMarketCodeRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.RawPublishPort;
import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;
import com.example.demo.ingestion.exchange.infrastruct.cache.MarketCodeValueCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MarketCodeRawHandlerTest {

    @Mock
    GetMarketCodeRaw getter;

    @Mock
    MarketCodeValueCache register;

    @Mock
    RawPublishPort<MarketCodeRawMessage> publisher;

    @Mock
    ParseMarketCodeRawUseCase parser;

    @InjectMocks
    MarketCodeRawHandler sut;

    @Test
    @DisplayName("process — getter → cache.put → publisher.publish 순서로 처리")
    void process_getsParsesCachesAndPublishes() {
        // given
        MarketCodeRawMessage raw1 = new MarketCodeRawMessage("Upbit", "SPOT", "KR", "KRW", "BTC", "KRW-BTC");
        MarketCodeRawMessage raw2 = new MarketCodeRawMessage("Upbit", "SPOT", "KR", "KRW", "ETH", "KRW-ETH");
        given(getter.getAll()).willReturn(List.of(raw1, raw2));

        MarketCodeKey key1 = new MarketCodeKey("Upbit", "KRW-BTC");
        MarketCodeKey key2 = new MarketCodeKey("Upbit", "KRW-ETH");
        MarketCodeValue val1 = new MarketCodeValue("SPOT", "KRW", "BTC");
        MarketCodeValue val2 = new MarketCodeValue("SPOT", "KRW", "ETH");

        given(parser.getKey(raw1)).willReturn(key1);
        given(parser.getKey(raw2)).willReturn(key2);
        given(parser.getValue(raw1)).willReturn(val1);
        given(parser.getValue(raw2)).willReturn(val2);

        // when
        sut.process();

        // then
        then(register).should().put(key1, val1);
        then(register).should().put(key2, val2);
        then(publisher).should().publish(raw1);
        then(publisher).should().publish(raw2);
    }

    @Test
    @DisplayName("process — getter가 빈 리스트 반환 시 cache/publisher 미호출")
    void process_emptyList_noInteractions() {
        // given
        given(getter.getAll()).willReturn(List.of());

        // when
        sut.process();

        // then
        then(register).shouldHaveNoInteractions();
        then(publisher).shouldHaveNoInteractions();
    }
}
