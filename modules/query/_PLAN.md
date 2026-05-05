# Query 모듈 구현 계획

## 공통 원칙
- 조회 전용. write 모듈(analytics, market_data 등) 수정 없음.
- 외부 합성(JOIN, 혼합 응답)은 query 모듈에서 만들지 않는다.
  tick + premium 동기 시계열 등 다중 도메인 응답은 API 모듈이 두 Port 호출 후 timestamp로 zip하는 패턴으로 처리.
  **모듈 내부의 테이블 합성도 포함된다.**

---

## analytics_query

### 기능 분류

| 기능 | 구현 방식 | 비고 |
|------|-----------|------|
| Tick 캔들 시계열 | JPA | 범위 조회 |
| Premium 캔들 시계열 | JPA | 범위 조회 |
| PremiumDetail 캔들 raw 시계열 | JPA | 단일 테이블 단순 조회 |
| PremiumDetail 캔들 재계산용(lookback) | JPA | from을 lookbackBuckets만큼 앞당겨 조회 |
| Tick 캔들 미니차트 | JPA | Top N desc |
| Premium 캔들 미니차트 | JPA | Top N desc |
| Tick 캔들 다운샘플 | JDBC/SQL | TimescaleDB time_bucket |
| Premium 캔들 다운샘플 | JDBC/SQL | TimescaleDB time_bucket |
| Tick 지표 시계열 | JPA | 범위 조회 |
| Premium 지표 시계열 | JPA | 범위 조회 |
| Tick 최신 지표 단일 | JPA | Top 1 |
| Premium 최신 지표 단일 | JPA | Top 1 |
| Tick 최신 지표 멀티마켓 | JDBC/SQL | DISTINCT ON |
| Tick/Premium 스크리너 | QueryDSL | BooleanBuilder, 다중 조건 AND 교집합 |
| Tick 마지막 버킷 메타 | JPA | MAX(bucketCloseTs) |
| Premium 마지막 버킷 메타 | JPA | MAX(bucketCloseTs) |
| PremiumDetail 마지막 버킷 메타 | JPA | MAX(bucketCloseTs) |

### DTO 구조 (sealed interface 기반)

```
application/dto/
  CandleView (sealed)
    └─ TickCandleView (record)
    └─ PremiumCandleView (record)
    └─ PremiumDetailCandleView (record)
         └─ PremiumDetailOHLC (nested record: basePrice, baseQuoteVal, comparePrice, compareQuoteVal)
  IndicatorView (sealed)
    └─ TickIndicatorView (record)
    └─ PremiumIndicatorView (record)
  ScreenerResult (sealed)
    └─ TickScreenerResult (record)
    └─ PremiumScreenerResult (record)
  ScreenerCondition (record)
  LastBucketMeta (sealed)
    └─ TickLastBucketMeta (record)
    └─ PremiumLastBucketMeta (record)
    └─ PremiumDetailLastBucketMeta (record)
```

### Port/UseCase 목록 (종류별 1:1 분리)

```
port/out/
  GetTickCandleSeriesPort
  GetPremiumCandleSeriesPort
  GetTickCandleMiniChartPort
  GetPremiumCandleMiniChartPort
  GetTickCandleDownsampledPort
  GetPremiumCandleDownsampledPort
  GetPremiumDetailCandleSeriesPort
  GetPremiumDetailCandleForRecomputePort
  GetTickIndicatorSeriesPort
  GetPremiumIndicatorSeriesPort
  GetTickLatestIndicatorPort
  GetPremiumLatestIndicatorPort
  GetLatestIndicatorMultiMarketPort  (tick 전용)
  GetScreenerPort
  GetLastClosedBucketPort            (tick/premium/premiumDetail 3메서드)

usecase/
  GetTickCandleSeriesUseCase
  GetPremiumCandleSeriesUseCase
  GetTickCandleMiniChartUseCase
  GetPremiumCandleMiniChartUseCase
  GetTickCandleDownsampledUseCase
  GetPremiumCandleDownsampledUseCase
  GetPremiumDetailCandleSeriesUseCase
  GetPremiumDetailCandleForRecomputeUseCase  (lookback 산술: displayFromTs - buckets * ms)
  GetTickIndicatorSeriesUseCase
  GetPremiumIndicatorSeriesUseCase
  GetTickLatestIndicatorUseCase
  GetPremiumLatestIndicatorUseCase
  GetLatestIndicatorMultiMarketUseCase
  GetScreenerUseCase
  GetLastClosedBucketUseCase
```

### SQL 파일

```
resources/sql/
  candle_downsampled.sql          (tick, TimescaleDB time_bucket)
  premium_candle_downsampled.sql  (premium, TimescaleDB time_bucket)
  latest_indicator_multi_market.sql (tick DISTINCT ON)
```

---

## market_data_query
- Tick / Premium / PremiumDetail 최신 및 시계열 조회
- 별도 _PLAN 없이 구현 완료

## meta_data_query
- Exchange / MarketCode 마스터 조회
- 별도 _PLAN 없이 구현 완료

## economic_query
- 경제지표 조회
- 별도 _PLAN 없이 구현 완료
