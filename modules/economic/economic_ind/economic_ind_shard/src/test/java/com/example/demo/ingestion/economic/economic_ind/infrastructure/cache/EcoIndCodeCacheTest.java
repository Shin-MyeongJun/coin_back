package com.example.demo.ingestion.economic.economic_ind.infrastructure.cache;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EcoIndCodeCacheTest {

    EcoIndCodeCache sut;

    EconomicIndicatorCode code1 = EconomicIndicatorCode.of("US", "CPI", ReleaseFrequency.MONTHLY, IndicatorUnit.PERCENT);
    EconomicIndicatorCode code2 = EconomicIndicatorCode.of("KR", "GDP", ReleaseFrequency.QUARTERLY, IndicatorUnit.INDEX);

    @BeforeEach
    void setUp() {
        sut = new EcoIndCodeCache();
    }

    @Test
    @DisplayName("put(code, id) — getId와 getIndCode 양방향 조회")
    void put_single_bidirectionalLookup() {
        sut.put(code1, 1L);

        assertThat(sut.getId(code1)).isEqualTo(1L);
        assertThat(sut.getIndCode(1L)).isEqualTo(code1);
    }

    @Test
    @DisplayName("put(map) — 다수 항목 일괄 저장 후 양방향 조회")
    void put_map_bidirectionalLookup() {
        sut.put(Map.of(10L, code1, 20L, code2));

        assertThat(sut.getId(code1)).isEqualTo(10L);
        assertThat(sut.getId(code2)).isEqualTo(20L);
        assertThat(sut.getIndCode(10L)).isEqualTo(code1);
        assertThat(sut.getIndCode(20L)).isEqualTo(code2);
    }

    @Test
    @DisplayName("getId — 존재하지 않는 key는 null 반환")
    void getId_missingKey_returnsNull() {
        assertThat(sut.getId(code1)).isNull();
    }

    @Test
    @DisplayName("getIndCode — 존재하지 않는 id는 null 반환")
    void getIndCode_missingId_returnsNull() {
        assertThat(sut.getIndCode(999L)).isNull();
    }
}
