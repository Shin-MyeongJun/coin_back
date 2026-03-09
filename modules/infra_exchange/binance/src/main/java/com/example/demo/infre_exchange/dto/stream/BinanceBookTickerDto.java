package com.example.demo.infre_exchange.dto.stream;


import com.dslplatform.json.CompiledJson;

@CompiledJson
public record BinanceBookTickerDto  (
     String e,
     long u,   // 업데이트 ID
     long E,
     long T,
     String s, // 거래쌍
     String b, // 최우선 매수 호가
     String B, // 최우선 매수 수량
     String a, // 최우선 매도 호가
     String A // 최우선 매도 수량
){}
