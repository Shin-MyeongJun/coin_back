package com.example.demo.analystics.domain.domain.indicator;

public enum TradeIndicatorGroup {
    STREAMING,     // O(1) – 이전 상태 + 새로운 값만으로 갱신
    SLIDING_WINDOW// O(N) – 과거 N개 값(윈도우) 전체를 저장·순회
}

