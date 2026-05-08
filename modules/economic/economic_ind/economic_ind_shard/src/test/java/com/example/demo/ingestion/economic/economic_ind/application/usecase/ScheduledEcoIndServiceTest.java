package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ScheduledEcoIndServiceTest {

    @Mock LoadRawIndDataPort<String> loadPort;
    @Mock RawToDomain<String, EconomicRawIndicator> rawMapper;
    @Mock FlushAndSaveEconomicValuePort<EconomicRawIndicator> indSave;
    @Mock FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave;

    ScheduledEcoIndService<String> sut;

    EconomicIndicatorCode code1 = EconomicIndicatorCode.of("US", "CPI", ReleaseFrequency.MONTHLY, IndicatorUnit.PERCENT);
    EconomicIndicatorCode code2 = EconomicIndicatorCode.of("US", "GDP", ReleaseFrequency.QUARTERLY, IndicatorUnit.INDEX);

    EconomicRawIndicator ind1 = new EconomicRawIndicator(code1,
            new EconomicIndicatorValue(new BigDecimal("3.5"), "1700000000", 1_699_000_000L, 1_700_000_001L));
    EconomicRawIndicator ind2 = new EconomicRawIndicator(code2,
            new EconomicIndicatorValue(new BigDecimal("2.1"), "1700000000", 1_699_000_000L, 1_700_000_002L));

    @BeforeEach
    void setUp() {
        sut = new ScheduledEcoIndService<>(loadPort, rawMapper, indSave, codeSave) {};
    }

    @Test
    @DisplayName("process — getRaws 결과를 모두 매핑하고 codeSave·indSave에 saveAll 호출")
    void process_loadsAndMaps_thenSavesCodesAndInds() {
        given(loadPort.getRaws()).willReturn(List.of("raw1", "raw2"));
        given(rawMapper.toDomain(eq("raw1"), any())).willReturn(ind1);
        given(rawMapper.toDomain(eq("raw2"), any())).willReturn(ind2);

        sut.process();

        then(codeSave).should().saveAll(List.of(code1, code2));
        then(indSave).should().saveAll(List.of(ind1, ind2));
    }

    @Test
    @DisplayName("process — 빈 raw 목록이면 saveAll에 빈 리스트 전달")
    void process_emptyRaws_savesEmptyLists() {
        given(loadPort.getRaws()).willReturn(List.of());

        sut.process();

        then(codeSave).should().saveAll(List.of());
        then(indSave).should().saveAll(List.of());
    }
}
