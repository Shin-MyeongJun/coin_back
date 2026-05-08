package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ScheduledEcoScheduleServiceTest {

    @Mock LoadRawIndDataPort<String> loadPort;
    @Mock RawToDomain<String, EconomicSchedule> rawMapper;
    @Mock FlushAndSaveEconomicValuePort<EconomicSchedule> scheduleSave;
    @Mock FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave;

    ScheduledEcoScheduleService<String> sut;

    EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "NFP", ReleaseFrequency.MONTHLY, IndicatorUnit.COUNT);
    EconomicSchedule schedule1 = new EconomicSchedule("US_NFP_MONTHLY_1000", code, 1_000L);
    EconomicSchedule schedule2 = new EconomicSchedule("US_NFP_MONTHLY_2000", code, 2_000L);

    @BeforeEach
    void setUp() {
        sut = new ScheduledEcoScheduleService<>(loadPort, rawMapper, scheduleSave, codeSave) {};
    }

    @Test
    @DisplayName("process — getRaws 결과를 매핑 후 codeSave·scheduleSave에 saveAll 호출")
    void process_loadsAndMaps_thenSavesCodesAndSchedules() {
        given(loadPort.getRaws()).willReturn(List.of("raw1", "raw2"));
        given(rawMapper.toDomain(eq("raw1"), any())).willReturn(schedule1);
        given(rawMapper.toDomain(eq("raw2"), any())).willReturn(schedule2);

        sut.process();

        then(codeSave).should().saveAll(List.of(code, code));
        then(scheduleSave).should().saveAll(List.of(schedule1, schedule2));
    }
}
