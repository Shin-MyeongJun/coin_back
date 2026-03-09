package com.example.demo.infre_exchange.dto.stream;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public  record BinanceMarkPriceDto  (
    String e, // 이벤트 타입
    long E,   // 이벤트 시간
    String s, // 거래쌍
    String p, // 마크 가격
    String i, // 인덱스 가격
    String P, // 펀딩 비율
    String r, // 펀딩 비율
    long T   // 다음 펀딩 시간
){}
