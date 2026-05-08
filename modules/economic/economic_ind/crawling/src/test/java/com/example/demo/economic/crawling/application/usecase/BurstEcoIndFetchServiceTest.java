package com.example.demo.economic.crawling.application.usecase;

import com.example.demo.economic.crawling.application.port.out.FetchEcoValuePort;
import com.example.demo.economic.crawling.infrastructure.config.CrawlingEcoProperties;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.PublishEcoIndPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BurstEcoIndFetchServiceTest {

    @Mock FetchEcoValuePort.ForInvesting investingFetcher;
    @Mock FetchEcoValuePort.ForYahoo yahooFetcher;
    @Mock FlushAndSaveEconomicValuePort<EconomicRawIndicator> indSave;
    @Mock FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave;
    @Mock PublishEcoIndPort publisher;
    @Mock CrawlingEcoProperties properties;
    @Mock CrawlingEcoProperties.IndicatorConfig config;
    @Mock CrawlingEcoProperties.BurstConfig burstConfig;

    BurstEcoIndFetchService sut;

    @BeforeEach
    void setUp() {
        sut = new BurstEcoIndFetchService(investingFetcher, yahooFetcher, indSave, codeSave, publisher, properties);
    }

    @Test
    @DisplayName("process — investing에서 값 즉시 반환 시 저장·발행 후 종료")
    void process_investingReturnsValue_savesAndPublishes() {
        given(properties.findByType("CPI")).willReturn(config);
        given(properties.getBurst()).willReturn(burstConfig);
        given(burstConfig.getTimeoutMs()).willReturn(60_000L);
        given(config.getCountry()).willReturn("US");
        given(config.getType()).willReturn("CPI");
        given(config.getFrequency()).willReturn(ReleaseFrequency.MONTHLY);
        given(config.getUnit()).willReturn(IndicatorUnit.PERCENT);
        given(investingFetcher.fetchCurrentValue("CPI")).willReturn(Optional.of(new BigDecimal("3.5")));

        sut.process("CPI");

        then(codeSave).should().saveAll(anyList());
        then(indSave).should().saveAll(anyList());
        then(publisher).should().publish(any(EconomicRawIndicator.class));
        then(yahooFetcher).should(never()).fetchCurrentValue(any());
    }

    @Test
    @DisplayName("process — investing 빈 값, yahoo에서 값 반환 시 저장·발행")
    void process_investingEmpty_yahooReturnsValue_savesAndPublishes() {
        given(properties.findByType("NFP")).willReturn(config);
        given(properties.getBurst()).willReturn(burstConfig);
        given(burstConfig.getTimeoutMs()).willReturn(60_000L);
        given(config.getCountry()).willReturn("US");
        given(config.getType()).willReturn("NFP");
        given(config.getFrequency()).willReturn(ReleaseFrequency.MONTHLY);
        given(config.getUnit()).willReturn(IndicatorUnit.COUNT);
        given(investingFetcher.fetchCurrentValue("NFP")).willReturn(Optional.empty());
        given(yahooFetcher.fetchCurrentValue("NFP")).willReturn(Optional.of(new BigDecimal("200000")));

        sut.process("NFP");

        then(codeSave).should().saveAll(anyList());
        then(indSave).should().saveAll(anyList());
        then(publisher).should().publish(any(EconomicRawIndicator.class));
    }

    @Test
    @DisplayName("process — config가 없으면 fetcher를 호출하지 않음")
    void process_configNotFound_noFetcherCalls() {
        given(properties.findByType("UNKNOWN")).willReturn(null);

        sut.process("UNKNOWN");

        then(investingFetcher).should(never()).fetchCurrentValue(any());
        then(yahooFetcher).should(never()).fetchCurrentValue(any());
        then(indSave).should(never()).saveAll(any());
        then(publisher).should(never()).publish(any());
    }
}
