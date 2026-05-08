package com.example.demo.economic.crawling.application.usecase;

import com.example.demo.infra_shard.persistence.EntityToDomain;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.DynamicSchedulingPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoIndCodeCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoScheduleCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.repo.EcoIndCodeRepository;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.repo.EconomicScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class CrawlingScheduledScheduleServiceTest {

    @Mock EcoIndCodeRepository codeRepository;
    @Mock EconomicScheduleRepository scheduleRepository;
    @Mock EntityToDomain<EcoIndCodeEntity, EconomicIndicatorCode> codeMapper;
    @Mock EntityToDomain<EconomicScheduleEntity, EconomicSchedule> scheduleMapper;
    @Mock EcoIndCodeCache ecoIndCodeCache;
    @Mock EcoScheduleCache ecoScheduleCache;
    @Mock DynamicSchedulingPort dynamicSchedulingPort;

    CrawlingScheduledScheduleService sut;

    @BeforeEach
    void setUp() {
        // Explicit constructor to avoid @InjectMocks swapping the two EntityToDomain mocks (generic erasure)
        sut = new CrawlingScheduledScheduleService(
                codeRepository, scheduleRepository, codeMapper, scheduleMapper,
                ecoIndCodeCache, ecoScheduleCache, dynamicSchedulingPort);
    }

    @Test
    @DisplayName("process — 코드 캐시·스케줄 캐시 갱신 후 adjustSchedule 호출")
    void process_refreshesCaches_thenAdjustsSchedule() {
        EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "NFP", ReleaseFrequency.MONTHLY, IndicatorUnit.COUNT);
        EconomicSchedule schedule = new EconomicSchedule("US_NFP_MONTHLY_1000", code, 1_000L);

        // Set JPA-managed id via reflection (no setter, @GeneratedValue field)
        EcoIndCodeEntity codeEntity = EcoIndCodeEntity.builder()
                .indicatorCode("US_NFP_MONTHLY").country("US").type("NFP")
                .frequency(ReleaseFrequency.MONTHLY).unit(IndicatorUnit.COUNT).build();
        ReflectionTestUtils.setField(codeEntity, "id", 1L);

        EconomicScheduleEntity scheduleEntity = EconomicScheduleEntity.builder()
                .releaseCode("US_NFP_MONTHLY_1000").releaseDate(1_000L).build();
        ReflectionTestUtils.setField(scheduleEntity, "id", 10L);

        given(codeRepository.findAll()).willReturn(List.of(codeEntity));
        given(codeMapper.toDomain(codeEntity)).willReturn(code);
        given(scheduleRepository.findAllPendingSchedules()).willReturn(List.of(scheduleEntity));
        given(scheduleMapper.toDomain(scheduleEntity)).willReturn(schedule);

        sut.process();

        InOrder order = inOrder(ecoIndCodeCache, ecoScheduleCache, dynamicSchedulingPort);
        order.verify(ecoIndCodeCache).put(Map.of(1L, code));
        order.verify(ecoScheduleCache).put(Map.of(10L, schedule));
        order.verify(dynamicSchedulingPort).adjustSchedule();
    }

    @Test
    @DisplayName("process — 빈 DB 결과에도 adjustSchedule 호출")
    void process_emptyRepos_stillAdjustsSchedule() {
        given(codeRepository.findAll()).willReturn(List.of());
        given(scheduleRepository.findAllPendingSchedules()).willReturn(List.of());

        sut.process();

        then(ecoIndCodeCache).should().put(anyMap());
        then(ecoScheduleCache).should().put(anyMap());
        then(dynamicSchedulingPort).should().adjustSchedule();
    }
}
