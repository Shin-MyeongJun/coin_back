package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.mapper;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ScheduleState;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoIndCodeCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EconomicScheduleEntityMapperTest {

    @Mock
    EcoIndCodeCache cache;

    @InjectMocks
    EconomicScheduleEntityMapper sut;

    EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "NFP", ReleaseFrequency.MONTHLY, IndicatorUnit.COUNT);

    @Test
    @DisplayName("toEntity — cache에서 indCodeId 조회 후 모든 필드 매핑")
    void toEntity_usesCache_andMapsAllFields() {
        EconomicSchedule schedule = new EconomicSchedule("US_NFP_MONTHLY_1000", code, 1_000L);
        schedule.setState(ScheduleState.PENDING);
        schedule.setFetchedAt(999L);

        given(cache.getId(code)).willReturn(7L);

        EconomicScheduleEntity entity = sut.toEntity(schedule);

        assertThat(entity.getIndCodeId()).isEqualTo(7L);
        assertThat(entity.getReleaseDate()).isEqualTo(1_000L);
        assertThat(entity.getReleaseCode()).isEqualTo("US_NFP_MONTHLY_1000");
        assertThat(entity.getStatus()).isEqualTo(ScheduleState.PENDING);
        assertThat(entity.getFetchedAt()).isEqualTo(999L);
    }

    @Test
    @DisplayName("toDomain — cache에서 EconomicIndicatorCode 조회 후 모든 필드 매핑")
    void toDomain_usesCache_andMapsAllFields() {
        EconomicScheduleEntity entity = EconomicScheduleEntity.builder()
                .id(5L)
                .indCodeId(7L)
                .releaseCode("US_NFP_MONTHLY_2000")
                .releaseDate(2_000L)
                .status(ScheduleState.DONE)
                .build();

        given(cache.getIndCode(7L)).willReturn(code);

        EconomicSchedule schedule = sut.toDomain(entity);

        assertThat(schedule.getReleaseCode()).isEqualTo("US_NFP_MONTHLY_2000");
        assertThat(schedule.getReleaseDate()).isEqualTo(2_000L);
        assertThat(schedule.getCode()).isEqualTo(code);
    }
}
