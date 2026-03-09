package com.example.demo.infre_exchange.upbit.dto;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;
import java.util.List;

/**
 * Upbit WebSocket MyAsset 응답 매핑용 DTO
 */
@CompiledJson
public record UpbitMyAssetDto(
        String ty,                  // 타입 (예: "myAsset")
        String astuid,              // 자산 고유 식별자
        List<Asset> ast,            // 보유 자산 목록
        long asttms,                // 자산 타임스탬프 (밀리초)
        long tms,                   // 전체 응답 타임스탬프 (밀리초)
        String st                   // 스트림 타입 ("REALTIME")
) {
    /**
     * 보유 자산 단일 항목
     */
    @CompiledJson
    public  record Asset(
            String cu,              // 화폐 코드 (예: "KRW", "BTC")
            BigDecimal b,           // 주문 가능 수량
            BigDecimal l            // 주문에 묶인 수량
    ) {}
}
