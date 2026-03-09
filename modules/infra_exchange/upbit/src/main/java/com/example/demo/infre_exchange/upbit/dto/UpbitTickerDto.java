package com.example.demo.infre_exchange.upbit.dto;

import com.dslplatform.json.CompiledJson;

import java.math.BigDecimal;

/**
 * Upbit WebSocket Ticker 응답 매핑용 DTO
 */
@CompiledJson
public record UpbitTickerDto(
        String ty,          // type: 데이터 타입 (예: "ticker")
        String cd,          // code: 마켓 코드 (예: "KRW-BTC")
        BigDecimal op,      // opening_price: 시가
        BigDecimal hp,      // high_price: 고가
        BigDecimal lp,      // low_price: 저가
        BigDecimal tp,      // trade_price: 현재가
        BigDecimal pcp,     // prev_closing_price: 전일 종가
        String c,           // change: RISE/EVEN/FALL
        BigDecimal cp,      // change_price: 전일 대비 가격
        BigDecimal scp,     // signed_change_price: 전일 대비 가격(부호 포함)
        BigDecimal cr,      // change_rate: 전일 대비 등락률
        BigDecimal scr,     // signed_change_rate: 등락률(부호 포함)
        BigDecimal tv,      // trade_volume: 최근 거래량
        BigDecimal atv,     // acc_trade_volume: 누적 거래량(UTC 0시 기준)
        BigDecimal atv24h,  // acc_trade_volume_24h: 24시간 누적 거래량
        BigDecimal atp,     // acc_trade_price: 누적 거래대금
        BigDecimal atp24h,  // acc_trade_price_24h: 24시간 누적 거래대금
        String tdt,         // trade_date: 최근 거래일자 (UTC)
        String ttm,         // trade_time: 최근 거래시간 (UTC)
        long ttms,          // trade_timestamp: 체결 타임스탬프 (ms)
        String ab,          // ask_bid: 매도/매수 구분 ("ASK" or "BID")
        BigDecimal aav,     // acc_ask_volume: 누적 매도량
        BigDecimal abv,     // acc_bid_volume: 누적 매수량
        BigDecimal h52wp,   // highest_52_week_price: 52주 최고가
        String h52wdt,      // highest_52_week_date: 최고가 달성일
        BigDecimal l52wp,   // lowest_52_week_price: 52주 최저가
        String l52wdt,      // lowest_52_week_date: 최저가 달성일
        String ts,          // trade_status: 거래상태 (Deprecated)
        String ms,          // market_state: 마켓 상태 (PREVIEW, ACTIVE 등)
        String msfi,        // market_state_for_ios: iOS 마켓 상태 (Deprecated)
        Boolean its,        // is_trading_suspended: 거래 정지 여부 (Deprecated)
        String dd,          // delisting_date: 거래지원 종료일
        String mw,          // market_warning: 유의 종목 여부 (NONE, CAUTION)
        long tms,           // timestamp: 수신 시점 기준 타임스탬프 (ms)
        String st           // stream_type: SNAPSHOT or REALTIME
) {}
