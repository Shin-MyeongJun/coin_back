package com.example.demo.ingestion.fx.infrastructure.mapper;

import com.example.demo.contracts.message.fx.FxMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NaverRawFxMapperTest {

    @Test
    @DisplayName("Naver raw 응답을 base/compare 순서를 보존한 FX 메시지로 매핑한다")
    void mapsRawResponseWithBaseCompareOrder() {
        String raw = """
                {"country":[{},{"value":"1,325.50"}]}
                """;
        NaverRawFxMapper sut = new NaverRawFxMapper();

        FxMessage result = sut.toMessage(raw, Map.of("base", "USD", "compare", "KRW"));

        assertThat(result.fxPair()).isEqualTo("USD/KRW");
        assertThat(result.base()).isEqualTo("USD");
        assertThat(result.compare()).isEqualTo("KRW");
        assertThat(result.val()).isEqualByComparingTo("1325.50");
    }
}
