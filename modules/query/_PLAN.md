# _PLAN.md — query 모듈 작성 계획

작성일: 2026-05-05 | 상태: 계획 완료, 구현 미시작

---

## 공통 결정사항

| 항목 | 결정 |
|------|------|
| SqlLoader 위치 | `infra_shard` 공유 컴포넌트. 각 query 모듈은 SqlLoader를 DI받아 사용 |
| RowMapper 전략 | `DataClassRowMapper<T>` 우선. 컬럼 조인/변환 필요 시 수동 `RowMapper<T>` 작성 |
| 파라미터 바인딩 | `:named` 파라미터만 (`NamedParameterJdbcTemplate`). `?` 위치 파라미터 금지 |
| 패키지 오타 | `analystics`, `ingection`, `infre_exchange` — 기존 코드와 일관성 유지 |
| 헥사고날 구조 | Port = interface (`application/port/out/`), Adapter = `@Component` (`infrastructure/persistence/adapter/`) |
| DI | 생성자 주입만 (`@RequiredArgsConstructor` + `private final`) |
| 도메인 객체 | 불변 응답 → `record`. 상태 변경 필요 시 `class` + `@Getter` |
| QueryDSL 사용처 | 옵셔널 필터 다수, 동적 조건 조합 (스크리너, 자동완성) |

---

## 외부 합성 메모

- **혼합 도메인 응답은 query 모듈에서 만들지 않는다.**
- 예: `Tick + Premium + Indicator` 조합 응답 → 각 query 모듈의 UseCase를 호출 후 상위 API 모듈에서 합성.
- query 모듈 간 의존성 없음. 각 모듈은 독립적으로 DB만 바라본다.

---

## 분류 기준 요약

| 전략 | 조건 |
|------|------|
| `.sql` 외부화 | 30줄 이상 / 윈도우 함수 / CTE / TimescaleDB(`time_bucket`, `last`, `first`, `locf`) / `LATERAL` JOIN / 멀티 마켓 latest 패턴 |
| JPA Repository | 단일 테이블 단순 조회, 단건/리스트, 페이징, 단순 WHERE/ORDER BY |
| QueryDSL | 옵셔널 필터 다수, 동적 조건 조합 |

---

## 작성 순서

1. `meta_data_query` ← 기존 skeleton 존재, 단순 구조
2. `economic_query` ← 신설, 작은 규모
3. `analytics_query` ← SQL 파일 다수 존재, JPA 전환 재정렬 필요
4. `market_data_query` ← 가장 복잡, 기존 stub 정리 포함

---

## 1. meta_data_query

**패키지**: `com.example.demo.meta_data_query`

### 기능 분류

| 기능 | 전략 | 비고 |
|------|------|------|
| 거래소 리스트 | JPA | `get_all_exchange.sql` → JPA 전환, SQL 삭제 예정 |
| 거래소별 MarketCode 리스트 | JPA | `get_all_market_code.sql` → JPA 전환, SQL 삭제 예정 |
| MarketCode 검색/자동완성 | QueryDSL | 동적 LIKE + exchange 필터 |
| 거래소-마켓 매핑 정합성 체크 | `.sql` | exchange ↔ market_code 매핑 검증 |

### 파일 트리

```
modules/query/meta_data_query/
├── build.gradle                                                          [ DONE ]
└── src/main/
    ├── java/com/example/demo/meta_data_query/
    │   ├── application/
    │   │   ├── port/out/
    │   │   │   ├── GetExchangeListPort.java                              [ DONE ]  JPA
    │   │   │   ├── GetMarketCodesByExchangePort.java                     [ DONE ]  JPA
    │   │   │   ├── SearchMarketCodePort.java                             [ DONE ]  QueryDSL
    │   │   │   └── CheckMappingIntegrityPort.java                        [ DONE ]  .sql
    │   │   ├── usecase/
    │   │   │   ├── GetExchangeListUseCase.java                           [ DONE ]
    │   │   │   ├── GetMarketCodesByExchangeUseCase.java                  [ DONE ]
    │   │   │   ├── SearchMarketCodeUseCase.java                          [ DONE ]
    │   │   │   └── CheckMappingIntegrityUseCase.java                     [ DONE ]
    │   │   └── dto/
    │   │       ├── ExchangeView.java                                     [ DONE ]  record
    │   │       ├── MarketCodeView.java                                   [ DONE ]  record
    │   │       ├── MarketCodeSearchResult.java                           [ DONE ]  record
    │   │       └── MappingIntegrityResult.java                           [ DONE ]  record
    │   └── infrastructure/persistence/
    │       ├── config/
    │       │   └── QueryDslConfig.java                                   [ DONE ]  JPAQueryFactory @Bean
    │       ├── entity/
    │       │   ├── ExchangeQueryEntity.java                              [ DONE ]  @Entity @Immutable (읽기전용)
    │       │   └── MarketCodeQueryEntity.java                            [ DONE ]  @Entity @Immutable (읽기전용)
    │       ├── repo/
    │       │   ├── ExchangeJpaRepository.java                            [ DONE ]  JpaRepository
    │       │   └── MarketCodeJpaRepository.java                          [ DONE ]  JpaRepository
    │       ├── mapper/
    │       │   ├── ExchangeViewMapper.java                               [ DONE ]  EntityToDomain<ExchangeQueryEntity, ExchangeView>
    │       │   └── MarketCodeViewMapper.java                             [ DONE ]  EntityToDomain<MarketCodeQueryEntity, MarketCodeView>
    │       ├── adapter/
    │       │   ├── GetExchangeListAdapter.java                           [ DONE ]  @Component, JPA
    │       │   ├── GetMarketCodesByExchangeAdapter.java                  [ DONE ]  @Component, JPA
    │       │   ├── SearchMarketCodeAdapter.java                          [ DONE ]  @Component, QueryDSL
    │       │   └── CheckMappingIntegrityAdapter.java                     [ DONE ]  @Component, .sql + DataClassRowMapper
    │       └── querydsl/
    │           └── MarketCodeQueryDslRepository.java                     [ DONE ]  JPAQueryFactory, 동적 LIKE/IN 필터
    └── resources/sql/
        ├── get_all_exchange.sql                                          [ STUB ]  JPA로 대체 → 삭제 예정
        ├── get_all_market_code.sql                                       [ STUB ]  JPA로 대체 → 삭제 예정
        └── check_mapping_integrity.sql                                   [ DONE ]  NOT EXISTS 고아 레코드 탐지
```

**의존성**: `infra_shard`(SqlLoader), `contracts`, `spring-data-jpa`, `querydsl-jpa`

---

## 2. economic_query (신설)

**패키지**: `com.example.demo.economic_query`

### 기능 분류

| 기능 | 전략 | 비고 |
|------|------|------|
| 단일 지표 시계열 | JPA | `economic_indicator` range 조회 |
| 발표 캘린더 | JPA | 날짜 범위 WHERE |
| 지표 메타 | JPA | 단건 또는 전체 리스트 |
| 카테고리별 지표 리스트 | JPA | `type` WHERE |
| 지표 변화율 (LAG 윈도우) | `.sql` | `LAG()` 윈도우 함수 사용 |
| 자산-지표 상관계수 (사전계산 조회) | JPA | `asset_indicator_correlation` 단순 SELECT |

### 파일 트리

```
modules/query/economic_query/
├── build.gradle                                                          [ DONE ]  신규 생성
└── src/main/
    ├── java/com/example/demo/economic_query/
    │   ├── application/
    │   │   ├── port/out/
    │   │   │   ├── GetIndicatorSeriesPort.java                           [ DONE ]  JPA
    │   │   │   ├── GetEconomicCalendarPort.java                          [ DONE ]  JPA
    │   │   │   ├── GetIndicatorMetaPort.java                             [ DONE ]  JPA
    │   │   │   ├── GetIndicatorListByCategoryPort.java                   [ DONE ]  JPA
    │   │   │   ├── GetIndicatorChangeRatePort.java                       [ DONE ]  .sql (LAG 윈도우)
    │   │   │   └── GetCorrelationResultPort.java                         [ DONE ]  JPA
    │   │   ├── usecase/
    │   │   │   ├── GetIndicatorSeriesUseCase.java                        [ DONE ]
    │   │   │   ├── GetEconomicCalendarUseCase.java                       [ DONE ]
    │   │   │   ├── GetIndicatorMetaUseCase.java                          [ DONE ]
    │   │   │   ├── GetIndicatorListByCategoryUseCase.java                [ DONE ]
    │   │   │   ├── GetIndicatorChangeRateUseCase.java                    [ DONE ]
    │   │   │   └── GetCorrelationResultUseCase.java                      [ DONE ]
    │   │   └── dto/
    │   │       ├── IndicatorSeriesView.java                              [ DONE ]  record
    │   │       ├── EconomicCalendarView.java                             [ DONE ]  record
    │   │       ├── IndicatorMetaView.java                                [ DONE ]  record
    │   │       ├── IndicatorChangeRateView.java                          [ DONE ]  record
    │   │       └── CorrelationResultView.java                            [ DONE ]  record
    │   └── infrastructure/persistence/
    │       ├── entity/
    │       │   ├── EcoIndQueryEntity.java                                [ DONE ]  economic_indicator
    │       │   ├── EcoIndCodeQueryEntity.java                            [ DONE ]  economic_indicator_code
    │       │   ├── EconomicScheduleQueryEntity.java                      [ DONE ]  economic_schedule
    │       │   └── CorrelationResultQueryEntity.java                     [ DONE ]  asset_indicator_correlation
    │       ├── repo/
    │       │   ├── EcoIndJpaRepository.java                              [ DONE ]  JpaRepository
    │       │   ├── EcoIndCodeJpaRepository.java                          [ DONE ]  JpaRepository
    │       │   ├── EconomicScheduleJpaRepository.java                    [ DONE ]  JpaRepository
    │       │   └── CorrelationResultJpaRepository.java                   [ DONE ]  JpaRepository
    │       ├── mapper/
    │       │   └── IndicatorViewMapper.java                              [ DONE ]  entity→DTO 통합 매퍼
    │       └── adapter/
    │           ├── GetIndicatorSeriesAdapter.java                        [ DONE ]  @Component, JPA
    │           ├── GetEconomicCalendarAdapter.java                       [ DONE ]  @Component, JPA
    │           ├── GetIndicatorMetaAdapter.java                          [ DONE ]  @Component, JPA
    │           ├── GetIndicatorListByCategoryAdapter.java                [ DONE ]  @Component, JPA
    │           ├── GetIndicatorChangeRateAdapter.java                    [ DONE ]  @Component, .sql
    │           └── GetCorrelationResultAdapter.java                      [ DONE ]  @Component, JPA
    └── resources/sql/
        └── indicator_change_rate.sql                                     [ DONE ]  LAG() 윈도우, 전후값 변화율 계산
```

**의존성**: `infra_shard`(SqlLoader), `contracts`, `spring-data-jpa`

---

## 3. analytics_query

**패키지**: `com.example.demo.analytics_query`

> 기존 SQL 파일 다수 존재. 분류 기준에 따라 JPA 대상 SQL은 JPA로 전환 후 삭제.
> `latest_premium_indicator.sql`(CTE + ROW_NUMBER, 47줄) — `.sql` 유지 대상이었으나
> 단건 조회는 JPA `findTop1By...OrderBy...` 로 대체 가능. 멀티 마켓 latest만 `.sql` 유지.

### 기능 분류

| 기능 | 전략 | 기존 SQL 파일 | 비고 |
|------|------|--------------|------|
| 캔들 시계열 (interval 고정) | JPA | `range/candle/range_*_candle.sql` → 삭제 예정 | |
| 캔들 최신 N개 미니차트 | JPA | — | `findTop N By...OrderBy` |
| 캔들 다운샘플링 (on-the-fly) | `.sql` | — | `time_bucket` TimescaleDB |
| 지표 시계열 단일 마켓·파라미터 | JPA | `range/indicator/range_*_indicator.sql` → 삭제 예정 | |
| 지표 최신값 단건 | JPA | `latest/latest_*_indicator.sql` → 삭제 예정 | `findTop1` 으로 대체 |
| 지표 최신값 멀티 마켓 (스크리너용) | `.sql` | — | 여러 marketCodeId 동시 조회 |
| 스크리너 (동적 조건) | QueryDSL | — | RSI<X, EMA cross 등 동적 필터 |
| 분/시간/일봉 마지막 마감 시각 (메타) | JPA | — | `bucket_close_ts` MAX 단순 조회 |
| 캔들 + 김프 동기화 시리즈 | `.sql` | `range/multi/range_*_and_candle.sql` (현재 빈 파일) | tick/premium candle JOIN |

### 파일 트리

```
modules/query/analytics_query/
├── build.gradle                                                          [ DONE ]  과잉 의존성 → 최소 의존성으로 교체
└── src/main/
    ├── java/com/example/demo/analytics_query/
    │   ├── application/
    │   │   ├── port/out/
    │   │   │   ├── GetCandleSeriesPort.java                              [ DONE ]  JPA
    │   │   │   ├── GetCandleMiniChartPort.java                           [ DONE ]  JPA
    │   │   │   ├── GetCandleDownsampledPort.java                         [ DONE ]  .sql (time_bucket)
    │   │   │   ├── GetIndicatorSeriesPort.java                           [ DONE ]  JPA
    │   │   │   ├── GetLatestIndicatorPort.java                           [ DONE ]  JPA (단건)
    │   │   │   ├── GetLatestIndicatorMultiMarketPort.java                [ DONE ]  .sql (멀티 마켓)
    │   │   │   ├── GetScreenerPort.java                                  [ DONE ]  QueryDSL
    │   │   │   ├── GetLastClosedBucketPort.java                          [ DONE ]  JPA (MAX bucket_close_ts)
    │   │   │   └── GetCandleAndPremiumSeriesPort.java                    [ DONE ]  .sql (tick candle + premium JOIN)
    │   │   ├── usecase/
    │   │   │   ├── GetCandleSeriesUseCase.java                           [ DONE ]
    │   │   │   ├── GetCandleMiniChartUseCase.java                        [ DONE ]
    │   │   │   ├── GetCandleDownsampledUseCase.java                      [ DONE ]
    │   │   │   ├── GetIndicatorSeriesUseCase.java                        [ DONE ]
    │   │   │   ├── GetLatestIndicatorUseCase.java                        [ DONE ]
    │   │   │   ├── GetLatestIndicatorMultiMarketUseCase.java             [ DONE ]
    │   │   │   ├── GetScreenerUseCase.java                               [ DONE ]
    │   │   │   ├── GetLastClosedBucketUseCase.java                       [ DONE ]
    │   │   │   └── GetCandleAndPremiumSeriesUseCase.java                 [ DONE ]
    │   │   └── dto/
    │   │       ├── CandleView.java                                       [ DONE ]  record (tick/premium 통합)
    │   │       ├── IndicatorView.java                                    [ DONE ]  record (tick/premium 통합)
    │   │       ├── ScreenerResult.java                                   [ DONE ]  record
    │   │       ├── LastBucketMeta.java                                   [ DONE ]  record
    │   │       └── CandleAndPremiumView.java                             [ DONE ]  record
    │   └── infrastructure/persistence/
    │       ├── config/
    │       │   └── QueryDslConfig.java                                   [ DONE ]  JPAQueryFactory @Bean
    │       ├── entity/
    │       │   ├── TickCandleQueryEntity.java                            [ DONE ]  @Immutable
    │       │   ├── PremiumCandleQueryEntity.java                         [ DONE ]  @Immutable
    │       │   ├── TickIndicatorQueryEntity.java                         [ DONE ]  @Immutable
    │       │   └── PremiumIndicatorQueryEntity.java                      [ DONE ]  @Immutable
    │       ├── repo/
    │       │   ├── TickCandleJpaRepository.java                          [ DONE ]  JpaRepository
    │       │   ├── PremiumCandleJpaRepository.java                       [ DONE ]  JpaRepository + @Query
    │       │   ├── TickIndicatorJpaRepository.java                       [ DONE ]  JpaRepository + findTop1
    │       │   └── PremiumIndicatorJpaRepository.java                    [ DONE ]  JpaRepository + @Query
    │       ├── mapper/
    │       │   ├── CandleViewMapper.java                                 [ DONE ]
    │       │   └── IndicatorViewMapper.java                              [ DONE ]
    │       ├── adapter/
    │       │   ├── GetCandleSeriesAdapter.java                           [ DONE ]  @Component, JPA
    │       │   ├── GetCandleMiniChartAdapter.java                        [ DONE ]  @Component, JPA + Pageable
    │       │   ├── GetCandleDownsampledAdapter.java                      [ DONE ]  @Component, .sql
    │       │   ├── GetIndicatorSeriesAdapter.java                        [ DONE ]  @Component, JPA
    │       │   ├── GetLatestIndicatorAdapter.java                        [ DONE ]  @Component, JPA
    │       │   ├── GetLatestIndicatorMultiMarketAdapter.java             [ DONE ]  @Component, .sql
    │       │   ├── GetScreenerAdapter.java                               [ DONE ]  @Component, QueryDSL
    │       │   ├── GetLastClosedBucketAdapter.java                       [ DONE ]  @Component, JPA MAX
    │       │   └── GetCandleAndPremiumSeriesAdapter.java                 [ DONE ]  @Component, .sql
    │       └── querydsl/
    │           └── IndicatorQueryDslRepository.java                      [ DONE ]  JPAQueryFactory, 동적 지표 조건
    └── resources/sql/
        ├── candle_downsampled.sql                                        [ DONE ]  time_bucket 다운샘플링
        ├── latest_indicator_multi_market.sql                             [ DONE ]  DISTINCT ON 멀티 marketCodeId latest
        └── candle_and_premium_series.sql                                 [ DONE ]  tick_candle + premium JOIN
        (기존 range/*, latest/* 파일 9개 → JPA 전환 후 git rm 완료)
```

**의존성**: `infra_shard`(SqlLoader), `contracts`, `spring-data-jpa`, `querydsl-jpa`

---

## 4. market_data_query

**패키지**: `com.example.demo.market_data_query`

> 기존 Port stub(빈 인터페이스) 및 LatestView(빈 record) 내용 채워야 함.
> SQL 디렉토리 오타: `lastest/` → `latest/` 리네임 필요 (기존 파일 이동 포함).

### 기능 분류

| 기능 | 전략 | 기존 SQL 파일 | 비고 |
|------|------|--------------|------|
| 특정 마켓 최신 Tick 단건 | JPA | `lastest/latest_tick.sql` → 삭제 예정 (후행 쉼표 오류) | |
| 멀티 마켓 최신 Tick 벌크 | `.sql` | — | `DISTINCT ON` 또는 `LATERAL` |
| base별 거래소 김프 스냅샷 | `.sql` | `lastest/latest_by_base_all_exchanges.sql` → 오류 수정 후 유지 | SQL 문법 오류 있음 |
| 김프 시계열 다운샘플링 | `.sql` | — | `time_bucket` |
| PremiumDetail 시계열 raw | JPA | `range/range_premium_detail.sql` → 삭제 예정 | |
| PremiumDetail 시계열 집계 | `.sql` | — | 집계 SQL 신규 작성 |
| 김프 랭킹 top N (양/음 분리) | `.sql` | — | |
| FX 최신값 | JPA | — | |
| FX 시계열 raw | JPA | — | |
| FX 시계열 다운샘플링 | `.sql` | — | `time_bucket` |

### 파일 트리

```
modules/query/market_data_query/
├── build.gradle                                                          [ DONE ]  infra_shard + jdbc 추가
└── src/main/
    ├── java/com/example/demo/market_data_query/
    │   ├── application/
    │   │   ├── port/out/
    │   │   │   ├── GetLatestTickPort.java                               [ DONE ]  JPA (단건)
    │   │   │   ├── GetLatestTickBulkPort.java                           [ DONE ]  .sql (DISTINCT ON)
    │   │   │   ├── GetPremiumSnapshotByBasePort.java                    [ DONE ]  .sql (DISTINCT ON per exchange)
    │   │   │   ├── GetPremiumTimeSeriesPort.java                        [ DONE ]  .sql (time_bucket 다운샘플링)
    │   │   │   ├── GetPremiumDetailRawPort.java                         [ DONE ]  JPA
    │   │   │   ├── GetPremiumDetailAggPort.java                         [ DONE ]  .sql
    │   │   │   ├── GetPremiumRankingPort.java                           [ DONE ]  .sql (top N 양/음 분리)
    │   │   │   ├── GetLatestFxPort.java                                 [ DONE ]  JPA
    │   │   │   ├── GetFxRawPort.java                                    [ DONE ]  JPA
    │   │   │   └── GetFxDownsampledPort.java                            [ DONE ]  .sql (time_bucket)
    │   │   ├── usecase/
    │   │   │   ├── GetLatestTickUseCase.java                            [ DONE ]
    │   │   │   ├── GetLatestTickBulkUseCase.java                        [ DONE ]
    │   │   │   ├── GetPremiumSnapshotByBaseUseCase.java                 [ DONE ]
    │   │   │   ├── GetPremiumTimeSeriesUseCase.java                     [ DONE ]
    │   │   │   ├── GetPremiumDetailRawUseCase.java                      [ DONE ]
    │   │   │   ├── GetPremiumDetailAggUseCase.java                      [ DONE ]
    │   │   │   ├── GetPremiumRankingUseCase.java                        [ DONE ]
    │   │   │   ├── GetLatestFxUseCase.java                              [ DONE ]
    │   │   │   ├── GetFxRawUseCase.java                                 [ DONE ]
    │   │   │   └── GetFxDownsampledUseCase.java                         [ DONE ]
    │   │   └── dto/
    │   │       ├── RangeView.java                                       [ DONE ]
    │   │       ├── TickLatestView.java                                  [ DONE ]  record
    │   │       ├── TickBulkView.java                                    [ DONE ]  record
    │   │       ├── PremiumSnapshotView.java                             [ DONE ]  record
    │   │       ├── PremiumTimeSeriesView.java                           [ DONE ]  record
    │   │       ├── PremiumDetailView.java                               [ DONE ]  record
    │   │       ├── PremiumRankingView.java                              [ DONE ]  record
    │   │       └── FxView.java                                          [ DONE ]  record
    │   └── infrastructure/persistence/
    │       ├── entity/
    │       │   ├── TickQueryEntity.java                                 [ DONE ]  @Immutable
    │       │   ├── PremiumDetailQueryEntity.java                        [ DONE ]  @Immutable
    │       │   └── FxQueryEntity.java                                   [ DONE ]  @Immutable
    │       ├── repo/
    │       │   ├── TickJpaRepository.java                               [ DONE ]  JPA (findTop1)
    │       │   ├── PremiumDetailJpaRepository.java                      [ DONE ]  JPA (raw range)
    │       │   └── FxJpaRepository.java                                 [ DONE ]  JPA (latest + raw)
    │       ├── mapper/
    │       │   ├── TickViewMapper.java                                  [ DONE ]
    │       │   ├── PremiumViewMapper.java                               [ DONE ]
    │       │   └── FxViewMapper.java                                    [ DONE ]
    │       └── adapter/
    │           ├── GetLatestTickAdapter.java                            [ DONE ]  @Component, JPA
    │           ├── GetLatestTickBulkAdapter.java                        [ DONE ]  @Component, .sql
    │           ├── GetPremiumSnapshotByBaseAdapter.java                 [ DONE ]  @Component, .sql
    │           ├── GetPremiumTimeSeriesAdapter.java                     [ DONE ]  @Component, .sql
    │           ├── GetPremiumDetailRawAdapter.java                      [ DONE ]  @Component, JPA
    │           ├── GetPremiumDetailAggAdapter.java                      [ DONE ]  @Component, .sql
    │           ├── GetPremiumRankingAdapter.java                        [ DONE ]  @Component, .sql
    │           ├── GetLatestFxAdapter.java                              [ DONE ]  @Component, JPA
    │           ├── GetFxRawAdapter.java                                 [ DONE ]  @Component, JPA
    │           └── GetFxDownsampledAdapter.java                         [ DONE ]  @Component, .sql
    └── resources/sql/
        ├── latest/
        │   ├── latest_tick_bulk.sql                                     [ DONE ]  DISTINCT ON 멀티 마켓
        │   └── latest_premium_by_base.sql                               [ DONE ]  DISTINCT ON per (baseExchangeId, compareExchangeId)
        └── range/
            ├── range_premium_downsampled.sql                            [ DONE ]  time_bucket + last()
            ├── range_premium_detail_agg.sql                             [ DONE ]  time_bucket + avg(compare/base-1)
            ├── premium_ranking.sql                                      [ DONE ]  CTE + ROW_NUMBER 양/음 분리 UNION ALL
            └── range_fx_downsampled.sql                                 [ DONE ]  time_bucket + avg(rate)
```

**의존성**: `infra_shard`(SqlLoader), `contracts`, `spring-data-jpa`, `spring-boot-starter-jdbc`

---

## 다음 시작 지점

세션 복원용. 작업 완료 시 업데이트.

| 항목 | 내용 |
|------|------|
| **현재 작업 모듈** | 전체 완료 |
| **다음 할 일** | — |

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-05-05 | 최초 작성 |
| 2026-05-05 | meta_data_query 구현 완료 (entity, repo, mapper, adapter, querydsl, usecase, SQL) |
| 2026-05-05 | economic_query 구현 완료 (신설 모듈, settings.gradle 등록, 전 레이어 작성) |
| 2026-05-05 | analytics_query 구현 완료 (build.gradle 교체, 기존 SQL 9개 삭제, 전 레이어 작성) |
| 2026-05-05 | market_data_query 구현 완료 (build.gradle 갱신, usecase 10개, entity 3개, repo 3개, mapper 3개, adapter 10개, SQL 6개) |
