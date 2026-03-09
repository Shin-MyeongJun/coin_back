package com.example.demo.infre_exchange.dto.stream;

import com.dslplatform.json.CompiledJson;

@CompiledJson
 public record BinanceMiniTickerDto  (
     String e, // 이벤트 타입
     long E,   // 이벤트 시간
     String s, // 거래쌍
     String c, // 종가
     String o, // 시가
     String h, // 고가
     String l, // 저가
     String v, // 거래량
     String q // 거래 금액
){}
