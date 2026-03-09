package com.example.demo.analystics.infrastructure.cache;

import org.springframework.stereotype.Component;

@Component
public class CacheExpirationPolicy {

    private final long bufferSeconds =5;

    public long calculateTtl(long openTimeMillis,  long intervalSeconds) {
        long currentSeconds = System.currentTimeMillis() / 1000;

        // 다음 경계 시간 계산 (예: 현재 13시, 주기 4시간 -> 다음 경계 16시)
        long nextBoundary = ((openTimeMillis/ intervalSeconds) + 1) * intervalSeconds;

        long remaining = nextBoundary - currentSeconds;

        // 🛡️ [안전장치] 여유 시간(Buffer) 추가 (예: 5초)
        // 정각에 칼같이 사라지면, 0.001초 차이로 복구 못할 수도 있으므로 약간의 여유를 둡니다.
        return remaining + bufferSeconds;
    }
}
