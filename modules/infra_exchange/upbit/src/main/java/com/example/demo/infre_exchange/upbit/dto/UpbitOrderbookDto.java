package com.example.demo.infre_exchange.upbit.dto;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;
import java.util.List;

@CompiledJson
public record UpbitOrderbookDto(
        String ty,                  // 타입 (예: "orderbook")
        String cd,                  // 마켓 코드 (예: "KRW-BTC")
        long tms,                   // 타임스탬프 (밀리초)
        BigDecimal tas,             // 호가 매도 총 잔량
        BigDecimal tbs,             // 호가 매수 총 잔량
        List<OrderbookUnit> obu,    // 호가 리스트

        double lv                   // 호가 모아보기 단위
) {
    /**
     * 호가 단위 정보
     */
    @CompiledJson
    public record OrderbookUnit(
            BigDecimal ap,  // 매도 호가
            BigDecimal bp,  // 매수 호가
            BigDecimal as,  // 매도 잔량
            BigDecimal bs   // 매수 잔량
    ) {}
}