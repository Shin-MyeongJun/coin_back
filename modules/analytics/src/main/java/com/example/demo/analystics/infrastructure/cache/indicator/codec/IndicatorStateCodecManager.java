package com.example.demo.analystics.infrastructure.cache.indicator.codec;

import com.example.demo.analystics.domain.domain.indicator.TradeIndicatorType;
import com.example.demo.analystics.domain.domain.indicator.open.state.IndicatorState;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.base.IndicatorStateCodec;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IndicatorStateCodecManager {
    private final Map<TradeIndicatorType, IndicatorStateCodec<?>> codecMap;

    public IndicatorStateCodecManager(List<IndicatorStateCodec<?>> codecs) {
       codecMap = new HashMap<>();
        for (IndicatorStateCodec<?> codec : codecs) {
            for (TradeIndicatorType type : codec.supportedTradeIndicatorTypes()) {
                // 중복 체크 (선택 사항)
                if (codecMap.containsKey(type)) {
                    throw new IllegalStateException("Duplicate codec found for type: " + type);
                }
                codecMap.put(type, codec);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends IndicatorState> byte[] encode(TradeIndicatorType type, T state ,long timestamp) {
        IndicatorStateCodec<T> codec = (IndicatorStateCodec<T>) getCodec(type);
        return codec.encode(state,timestamp);
    }

    /**
     * 바이트 배열을 상태 객체(RecoveryIndicatorState)로 디코딩
     * (제네릭 메서드로 선언하여 타입 캐스팅을 내부에서 처리)
     */
    @SuppressWarnings("unchecked")
    public <T extends IndicatorState> RecoveryIndicatorState decode(TradeIndicatorType type, byte[] bytes) {
        IndicatorStateCodec<T> codec = (IndicatorStateCodec<T>) getCodec(type);
        return codec.decode(bytes);
    }

    /**
     * 코덱 조회 및 예외 처리 헬퍼 메서드
     */
    private IndicatorStateCodec<?> getCodec(TradeIndicatorType type) {
        IndicatorStateCodec<?> codec = codecMap.get(type);
        if (codec == null) {
            throw new IllegalArgumentException("지원하지 않는 인디케이터 타입입니다: " + type);
        }
        return codec;
    }

}
