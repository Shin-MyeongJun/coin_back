package com.example.demo.infre_exchange.upbit.dto;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;

@CompiledJson
public record UpbitMyOrderDto(
        String ty,          // 타입 (예: "myOrder")
        String cd,          // 마켓 코드 (예: "KRW-BTC")
        String uid,         // 주문 고유 아이디
        String ab,          // 매수/매도 구분 ("ASK", "BID")
        String ot,          // 주문 타입 ("limit", "price", "market", "best")
        String s,           // 주문 상태 ("wait", "watch", "trade", "done", "cancel")
        String tuid,        // 체결의 고유 아이디
        BigDecimal p,       // 주문 가격 또는 체결 가격
        BigDecimal ap,      // 평균 체결 가격
        BigDecimal v,       // 주문량 또는 체결량
        BigDecimal rv,      // 체결 후 남은 주문 양
        BigDecimal ev,      // 체결된 양
        int tc,             // 해당 주문에 걸린 체결 수
        BigDecimal rsf,     // 수수료로 예약된 비용
        BigDecimal rmf,     // 남은 수수료
        BigDecimal pf,      // 사용된 수수료
        BigDecimal l,       // 거래에 사용 중인 비용
        BigDecimal ef,      // 체결된 금액
        String tif,         // IOC, FOK 설정 ("ioc", "fok")
        BigDecimal tf,      // 체결 시 발생한 수수료
        Boolean im,         // 메이커 여부 (true: 메이커, false: 테이커)
        String id,          // 조회용 사용자 지정값
        long ttms,          // 체결 타임스탬프 (밀리초)
        long otms,          // 주문 타임스탬프 (밀리초)
        long tms,           // 타임스탬프 (밀리초)
        String st           // 스트림 타입 ("REALTIME")
) {}

