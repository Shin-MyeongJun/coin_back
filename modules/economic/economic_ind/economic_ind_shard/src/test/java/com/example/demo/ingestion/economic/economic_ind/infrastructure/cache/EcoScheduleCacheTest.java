package com.example.demo.ingestion.economic.economic_ind.infrastructure.cache;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EcoScheduleCacheTest {

    EcoScheduleCache sut;

    EconomicIndicatorCode code = EconomicIndicatorCode.of("US", "NFP", ReleaseFrequency.MONTHLY, IndicatorUnit.COUNT);
    EconomicSchedule schedule1 = new EconomicSchedule("US_NFP_MONTHLY_1000", code, 1_000L);
    EconomicSchedule schedule2 = new EconomicSchedule("US_NFP_MONTHLY_2000", code, 2_000L);

    @BeforeEach
    void setUp() {
        sut = new EcoScheduleCache();
    }

    @Test
    @DisplayName("put(schedule, id) — getId와 getSchedule 양방향 조회")
    void put_single_bidirectionalLookup() {
        sut.put(schedule1, 1L);

        assertThat(sut.getId(schedule1)).isEqualTo(1L);
        assertThat(sut.getSchedule(1L)).isSameAs(schedule1);
    }

    @Test
    @DisplayName("put(map) — 다수 항목 일괄 저장 후 양방향 조회")
    void put_map_bidirectionalLookup() {
        sut.put(Map.of(10L, schedule1, 20L, schedule2));

        assertThat(sut.getId(schedule1)).isEqualTo(10L);
        assertThat(sut.getId(schedule2)).isEqualTo(20L);
        assertThat(sut.getSchedule(10L)).isSameAs(schedule1);
    }

    @Test
    @DisplayName("getAllSchedule — 저장된 모든 스케줄 반환")
    void getAllSchedule_returnsAllStored() {
        sut.put(Map.of(10L, schedule1, 20L, schedule2));

        assertThat(sut.getAllSchedule()).hasSize(2).containsExactlyInAnyOrder(schedule1, schedule2);
    }

    @Test
    @DisplayName("getId — 존재하지 않는 key는 null 반환")
    void getId_missingKey_returnsNull() {
        assertThat(sut.getId(schedule1)).isNull();
    }
}
