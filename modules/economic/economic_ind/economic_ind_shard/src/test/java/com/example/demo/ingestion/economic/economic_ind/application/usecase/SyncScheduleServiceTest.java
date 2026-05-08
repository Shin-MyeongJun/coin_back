package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadEcoIndCodePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadScheduledEcoPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class SyncScheduleServiceTest {

    @Mock ReadScheduledEcoPort readScheduledPort;
    @Mock ReadEcoIndCodePort readEcoIndCodePort;
    @Mock FlushAndSaveEconomicValuePort<EconomicIndicatorCode> writeEcoIndCodePort;
    @Mock FlushAndSaveEconomicValuePort<EconomicSchedule> writeSchedulePort;
    @Mock LoadRawIndDataPort<String> getters;
    @Mock RawToDomain<String, EconomicSchedule> rawToDomain;

    SyncScheduleService<String> sut;

    EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "NFP", ReleaseFrequency.MONTHLY, IndicatorUnit.COUNT);
    EconomicSchedule schedule = new EconomicSchedule("US_NFP_MONTHLY_1000", code, 1_000L);

    @BeforeEach
    void setUp() {
        sut = new SyncScheduleService<>(
                readScheduledPort, readEcoIndCodePort,
                writeEcoIndCodePort, writeSchedulePort,
                getters, rawToDomain) {};
    }

    @Test
    @DisplayName("sync — 외부데이터 저장 후 DB 재조회 순서 보장")
    void sync_savesSchedulesAndCodes_thenReadsBack() {
        given(getters.getRaws()).willReturn(List.of("raw1"));
        given(rawToDomain.toDomain(any(), any())).willReturn(schedule);
        given(readScheduledPort.readPending()).willReturn(List.of(schedule));
        given(readEcoIndCodePort.readAll()).willReturn(List.of(code));

        sut.sync();

        InOrder order = inOrder(writeSchedulePort, writeEcoIndCodePort, readScheduledPort, readEcoIndCodePort);
        order.verify(writeSchedulePort).saveAll(List.of(schedule));
        order.verify(writeEcoIndCodePort).saveAll(List.of(code));
        order.verify(readScheduledPort).readPending();
        order.verify(readEcoIndCodePort).readAll();
    }

    @Test
    @DisplayName("sync — 빈 raw 목록이면 saveAll에 빈 리스트 전달 후 DB 재조회")
    void sync_emptyRaws_savesEmptyListsThenReadsBack() {
        given(getters.getRaws()).willReturn(List.of());
        given(readScheduledPort.readPending()).willReturn(List.of());
        given(readEcoIndCodePort.readAll()).willReturn(List.of());

        sut.sync();

        then(writeSchedulePort).should().saveAll(List.of());
        then(writeEcoIndCodePort).should().saveAll(List.of());
        then(readScheduledPort).should().readPending();
        then(readEcoIndCodePort).should().readAll();
    }
}
