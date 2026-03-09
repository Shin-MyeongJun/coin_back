package com.example.demo.infre_exchange.dto.stream;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public record BinanceAggTradeDto (
     String e, // 이벤트 타입
     long E,   // 이벤트 시간
     String s, // 거래쌍
     long a,   // 집계된 거래 ID
     String p, // 가격
     String q, // 수량
     long f,   // 첫 번째 거래 ID
     long l,   // 마지막 거래 ID
     long T,   // 거래 시간
     boolean m, // 매수자 메이커 여부
     boolean M // 시장 일치 여부
){}