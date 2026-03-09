package com.example.demo.infre_exchange.dto.stream;

import com.dslplatform.json.CompiledJson;

@CompiledJson
 public record BinanceKlineDto  (
     String e, // 이벤트 타입
     long E,   // 이벤트 시간
     String s, // 거래쌍
     Kline k  // 캔들 데이터
) {
    @CompiledJson
    public  record Kline(
            long t,     // 캔들 시작 시간
            long T,     // 캔들 종료 시간
            String s,   // 거래쌍
            String i,   // 간격
            long f,     // 첫 번째 거래 ID
            long L,     // 마지막 거래 ID
            String o,   // 시가
            String c,   // 종가
            String h,   // 고가
            String l,   // 저가
            String v,   // 거래량
            long n,     // 거래 수
            boolean x,  // 캔들 종료 여부
            String q,   // 거래 금액
            String V,   // 매수 거래량
            String Q,   // 매수 거래 금액
            String B   // 무시 필드
    ) {
    }
}
