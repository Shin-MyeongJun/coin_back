# _PLAN.md — api 모듈 작성 계획

작성일: 2026-05-06 | 상태: 계획 완료, 구현 미시작

---

## 공통 결정사항

| 항목 | 결정 |
|------|------|
| 모듈 구성 | 단일 `api` 모듈. 내부에서 도메인별 패키지로 격리 (`controller/meta/`, `controller/market/`, …) |
| 모듈 위치 | `modules/api/` (오타 컨벤션 미적용, 정자 `api` 사용) |
| 패키지 base | `com.example.demo.api` |
| 의존성 | query/* + contracts + infra_shard + infra_heartbeat 만. 도메인 모듈(market_data, analytics, meta_data, ingestion/*, economic/*, trading) 직접 의존 금지 |
| 헥사고날 구조 | 본 모듈은 **어댑터 레이어**. UseCase 정의는 query 모듈에 있고, api 모듈은 그 UseCase를 호출하는 컨트롤러 + 합성 서비스만 보유 |
| DI | 생성자 주입만 (`@RequiredArgsConstructor` + `private final`) |
| 응답 객체 | 불변 → `record`. 응답 envelope/페이징 wrapper도 `record` |
| 인증·인가 | 1단계: `SecurityFilterChain` 빈만 두고 `permitAll()`. CSRF disable, CORS dev 허용. 2단계(외부 클라이언트): API Key. 3단계(트레이딩): JWT |
| 실시간 채널 | SSE (`SseEmitter`) + `Sinks.Many<T>` fanout. WebSocket은 미도입, 필요 시 같은 Sink 구독으로 추가 |
| Kafka 구독 | api 모듈이 Kafka consumer 직접 소유 (조회 전용 그룹). 다중 인스턴스 스케일아웃 시 Redis Pub/Sub 브릿지로 전환 검토 |
| 응답 컨벤션 — 페이징 | 시계열 = cursor (`?cursor={epochMs}&limit=`), 메타/리스트 = offset (`?page=&size=`) |
| 응답 컨벤션 — 시간 | epoch milliseconds (long). UTC 단일 기준 |
| 응답 컨벤션 — 에러 | RFC 7807 Problem Details (`ProblemDetail`). `@RestControllerAdvice` 글로벌 핸들러 |
| 응답 컨벤션 — envelope | 단건/리스트 raw, 페이징만 `{items, nextCursor, hasMore}` envelope |
| OpenAPI | `springdoc-openapi-starter-webmvc-ui`. dev 프로파일에서만 swagger-ui 노출 |
| 시크릿 관리 | `application.yml`에 시크릿 직접 노출 금지. 환경 변수 또는 외부 설정 (`${...}` placeholder) |

---

## 안전장치 (구조적 결정 — 추후 확장 비용을 0으로 유지하기 위함)

### 안전장치 1: 도메인별 패키지 격리
컨트롤러/합성 서비스/응답 DTO를 **반드시 도메인별 패키지**로 분리한다. 향후 트레이딩/거래소 API 추가 시 또는 모듈 분할 결정 시, 패키지 단위로 잘라 옮기기 위함.

```
api/controller/{meta,market,analytics,economic,trading,...}/
api/composition/{service,dto}/{market,analytics,...}/
api/stream/handler/{tick,premium,candle,...}/
```

### 안전장치 2: Stream Sink 격리
SSE 컨트롤러 안에 Kafka 구독·broadcast 로직을 **절대 박지 않는다**. `stream/` 패키지에 채널-무관 fanout 허브(`MarketDataStream`, `AnalyticsStream`)를 두고, SSE는 그 Sink를 `subscribe()`만 한다. 추후 WebSocket 추가 시 같은 Sink를 또 다른 핸들러가 구독하면 끝.

```
[Kafka Consumer] → [Sinks.Many<Domain>] → [SSE Emitter]
                                       ↘  [WebSocketHandler]   ← 향후 추가
```

Sink 시그니처는 **도메인 객체 기반** (`Sinks.Many<Premium>`). 직렬화는 채널 핸들러에서 수행.

---

## 작성 순서

1. **skeleton** ← settings 등록, build.gradle, ApiApplication, 공통 config (Security/OpenAPI/CORS/Exception/Jackson)
2. **meta 컨트롤러** ← 가장 단순. `meta_data_query` UseCase 1:1 매핑
3. **economic 컨트롤러** ← 단순 시계열, cursor 페이징 패턴 정착
4. **analytics 컨트롤러** ← 캔들/지표 시계열 + 스크리너
5. **market 컨트롤러** ← 단일 도메인 엔드포인트 (FX, 김프, 틱)
6. **composition 합성 엔드포인트** ← Tick + Premium + Indicator 합성, 멀티 query 모듈 호출
7. **stream (SSE)** ← Kafka consumer + Sink + SseEmitter

각 단계 완료 후 다음 단계 진입. 1~2 끝나면 프런트가 mock 없이 실제 호출 시작 가능.

---

## 1. skeleton ✅ DONE

**모듈 경로**: `modules/api/`
**패키지**: `com.example.demo.api`

### 파일 트리

```
modules/api/
├── build.gradle                                                          [DONE]
└── src/main/
    ├── java/com/example/demo/api/
    │   ├── ApiApplication.java                                           [DONE]  @SpringBootApplication, main
    │   └── config/
    │       ├── SecurityConfig.java                                       [DONE]  SecurityFilterChain, permitAll, CSRF disable
    │       ├── CorsConfig.java                                           [DONE]  dev 프로파일 와일드카드, prod 화이트리스트
    │       ├── OpenApiConfig.java                                        [DONE]  springdoc, dev 프로파일에서만 swagger-ui
    │       ├── JacksonConfig.java                                        [DONE]  epoch ms 시간 직렬화, BigDecimal 문자열
    │       ├── GlobalExceptionHandler.java                               [DONE]  @RestControllerAdvice, RFC 7807 ProblemDetail
    │       └── HeartbeatConfig.java                                      [DONE]  ModuleName.API 헬스 등록 (@ConditionalOnProperty)
    └── resources/
        ├── application.yml                                               [DONE]  base 설정, ${...} placeholder만
        ├── application-local.yml                                         [DONE]  로컬 오버라이드
        └── application-dev.yml                                           [DONE]  dev 프로파일 (swagger-ui 노출 등)
```

### settings.gradle 변경 (별도 표시)

- `settings.gradle` ← `:api` 이미 등록되어 있음 (변경 불필요)

### infra_heartbeat 변경 완료

- `ModuleName.java` ← `API("API")` 추가 완료
- `Health.java` ← `record`에 잘못 적용된 `@Getter`/`@Setter` 제거 (pre-existing 버그 수정)

**의존성** (build.gradle 최소 구성):
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `project(':infra_shard')`, `project(':infra_heartbeat')`, `project(':contracts')`
- `project(':query:meta_data_query')`, `project(':query:market_data_query')`, `project(':query:analytics_query')`, `project(':query:economic_query')`
- (스트림 단계에 진입할 때 추가) `org.springframework.kafka:spring-kafka`, `io.projectreactor:reactor-core`

---

## 2. meta 컨트롤러 ✅ DONE

**호출 대상**: `meta_data_query` UseCase 4종

### 엔드포인트 매핑

| 메서드 | 경로 | UseCase | 비고 |
|--------|------|---------|------|
| GET | `/api/v1/meta/exchanges` | `GetExchangeListUseCase` | 단순 리스트 |
| GET | `/api/v1/meta/exchanges/{exchangeId}/markets` | `GetMarketCodesByExchangeUseCase` | 단순 리스트 |
| GET | `/api/v1/meta/markets/search` | `SearchMarketCodeUseCase` | 자동완성, `?q=&exchange=` |
| GET | `/api/v1/meta/integrity` | `CheckMappingIntegrityUseCase` | 운영 점검용 |

### 파일 트리

```
modules/api/src/main/java/com/example/demo/api/
└── controller/meta/
    ├── ExchangeController.java                                           [DONE]
    ├── MarketCodeController.java                                         [DONE]
    └── MetaIntegrityController.java                                      [DONE]
```

응답 DTO는 `meta_data_query`의 View record를 그대로 반환. api 모듈이 자체 DTO를 만들지 않는다 (단순 1:1 패스스루).

---

## 3. economic 컨트롤러 ✅ DONE

**호출 대상**: `economic_query` UseCase 6종

### 엔드포인트 매핑

| 메서드 | 경로 | UseCase | 비고 |
|--------|------|---------|--------|
| GET | `/api/v1/economic/indicators/{codeId}/series` | `GetIndicatorSeriesUseCase` | ?fromTs=&toTs= |
| GET | `/api/v1/economic/calendar` | `GetEconomicCalendarUseCase` | ?fromTs=&toTs= |
| GET | `/api/v1/economic/indicators/{codeId}` | `GetIndicatorMetaUseCase` | 단건 |
| GET | `/api/v1/economic/indicators` | `GetIndicatorListByCategoryUseCase` or All | ?category= |
| GET | `/api/v1/economic/indicators/{codeId}/change-rate` | `GetIndicatorChangeRateUseCase` | raw list |
| GET | `/api/v1/economic/correlation` | `GetCorrelationResultUseCase` | ?asset= |

### 파일 트리

```
modules/api/src/main/java/com/example/demo/api/
├── common/
│   ├── CursorPage.java                                                   [DONE]  record (items, nextCursor, hasMore)
│   └── OffsetPage.java                                                   [DONE]  record (items, page, size, total)
└── controller/economic/
    ├── IndicatorController.java                                          [DONE]  series, meta, list, change-rate
    ├── EconomicCalendarController.java                                   [DONE]
    └── CorrelationController.java                                        [DONE]
```

---

## 4. analytics 컨트롤러 ✅ DONE

**호출 대상**: `analytics_query` UseCase 9종 (GetCandleAndPremiumSeriesUseCase는 리팩토링으로 삭제됨 — 제외)

### 엔드포인트 매핑

| 메서드 | 경로 | UseCase | 비고 |
|--------|------|---------|------|
| GET | `/api/v1/analytics/candles` | tick/premium Series | ?type=tick\|premium |
| GET | `/api/v1/analytics/candles/mini` | tick/premium MiniChart | ?limit= |
| GET | `/api/v1/analytics/candles/downsampled` | tick/premium Downsampled | ?targetBucketSeconds= |
| GET | `/api/v1/analytics/candles/last-closed` | GetLastClosedBucketUseCase | ?type=tick\|premium\|premium-detail |
| GET | `/api/v1/analytics/indicators` | tick/premium IndicatorSeries | ?type=tick\|premium |
| GET | `/api/v1/analytics/indicators/latest` | tick/premium LatestIndicator | 단건 |
| GET | `/api/v1/analytics/indicators/latest/multi` | GetLatestIndicatorMultiMarketUseCase | ?marketCodeIds=1,2,3 |
| GET | `/api/v1/analytics/screener` | GetScreenerUseCase | ?type=tick\|premium, single condition |

### 파일 트리

```
modules/api/src/main/java/com/example/demo/api/
└── controller/analytics/
    ├── CandleController.java                                             [DONE]
    ├── IndicatorController.java                                          [DONE]
    └── ScreenerController.java                                           [DONE]
```

---

## 5. market 컨트롤러

**호출 대상**: `market_data_query` UseCase 10종

### 엔드포인트 매핑

| 메서드 | 경로 | UseCase | 비고 |
|--------|------|---------|------|
| GET | `/api/v1/market/ticks/latest/{marketCodeId}` | `GetLatestTickUseCase` | 단건 |
| GET | `/api/v1/market/ticks/latest` | `GetLatestTickBulkUseCase` | `?marketCodeIds=` |
| GET | `/api/v1/market/premium/snapshot/{base}` | `GetPremiumSnapshotByBaseUseCase` | base별 거래소 스냅샷 |
| GET | `/api/v1/market/premium/series` | `GetPremiumTimeSeriesUseCase` | cursor + bucket |
| GET | `/api/v1/market/premium-detail/raw` | `GetPremiumDetailRawUseCase` | cursor |
| GET | `/api/v1/market/premium-detail/agg` | `GetPremiumDetailAggUseCase` | cursor + bucket |
| GET | `/api/v1/market/premium/ranking` | `GetPremiumRankingUseCase` | top N, `?direction=positive|negative&n=` |
| GET | `/api/v1/market/fx/latest` | `GetLatestFxUseCase` | `?currency=` |
| GET | `/api/v1/market/fx/raw` | `GetFxRawUseCase` | cursor |
| GET | `/api/v1/market/fx/downsampled` | `GetFxDownsampledUseCase` | cursor + bucket |

### 파일 트리

```
modules/api/src/main/java/com/example/demo/api/
└── controller/market/
    ├── TickController.java                                               [ TODO ]
    ├── PremiumController.java                                            [ TODO ]  snapshot, series, ranking
    ├── PremiumDetailController.java                                      [ TODO ]  raw, agg
    └── FxController.java                                                 [ TODO ]
```

---

## 6. composition (합성 엔드포인트)

**책임**: 여러 query UseCase를 호출해 혼합 응답 생성. `_PLAN.md` 정책 「혼합 도메인 응답은 query 모듈에서 만들지 않는다」의 위치.

### 엔드포인트 (예시 — 프런트 요구 명확해질 때 추가)

| 메서드 | 경로 | 합성 |
|--------|------|------|
| GET | `/api/v1/compose/market-overview/{marketCodeId}` | `GetLatestTick` + `GetPremiumSnapshotByBase` + `GetLatestIndicator` |
| GET | `/api/v1/compose/chart/{marketCodeId}` | `GetCandleSeriesUseCase` + `GetIndicatorSeriesUseCase` (interval 동기화) |
| GET | `/api/v1/compose/dashboard` | 다중 마켓 latest tick + premium ranking + 경제 캘린더 (홈 화면용) |

### 파일 트리

```
modules/api/src/main/java/com/example/demo/api/
└── composition/
    ├── service/
    │   ├── MarketOverviewService.java                                    [ TODO ]  여러 UseCase 호출 + 조합
    │   ├── ChartCompositionService.java                                  [ TODO ]
    │   └── DashboardService.java                                         [ TODO ]
    ├── dto/
    │   ├── MarketOverviewResponse.java                                   [ TODO ]  record
    │   ├── ChartResponse.java                                            [ TODO ]  record
    │   └── DashboardResponse.java                                        [ TODO ]  record
    └── controller/
        ├── MarketOverviewController.java                                 [ TODO ]
        ├── ChartController.java                                          [ TODO ]
        └── DashboardController.java                                      [ TODO ]
```

> 합성 엔드포인트는 프런트가 실제 화면을 그릴 때 요구사항 도출. 지금 단계에선 placeholder만, 단계 6 진입 시 구체화.

---

## 7. stream (SSE)

**책임**: Kafka에 흐르는 실시간 이벤트(tick, premium, candle close, indicator close)를 SSE로 클라이언트에 푸시.

### 엔드포인트

| 메서드 | 경로 | 이벤트 |
|--------|------|--------|
| GET | `/api/v1/stream/ticks` | `tick` 토픽 (쿼리: `?marketCodeId=`) |
| GET | `/api/v1/stream/premium` | `market-data.premium` 토픽 |
| GET | `/api/v1/stream/candles/close` | `analytics.tick-candle-close` 등 |
| GET | `/api/v1/stream/indicators/close` | `analytics.tick-indicator-close` 등 |

### 파일 트리

```
modules/api/src/main/java/com/example/demo/api/
└── stream/
    ├── config/
    │   └── StreamKafkaConfig.java                                        [ TODO ]  consumer factory, 조회 전용 group-id
    ├── consumer/
    │   ├── TickStreamConsumer.java                                       [ TODO ]  @KafkaListener → MarketDataStream
    │   ├── PremiumStreamConsumer.java                                    [ TODO ]
    │   ├── CandleCloseStreamConsumer.java                                [ TODO ]
    │   └── IndicatorCloseStreamConsumer.java                             [ TODO ]
    ├── sink/
    │   ├── MarketDataStream.java                                         [ TODO ]  Sinks.Many<Tick>, Sinks.Many<Premium>
    │   └── AnalyticsStream.java                                          [ TODO ]  Sinks.Many<CloseCandle>, Sinks.Many<CloseIndicator>
    ├── handler/
    │   ├── TickSseHandler.java                                           [ TODO ]  SseEmitter, 필터링(marketCodeId)
    │   ├── PremiumSseHandler.java                                        [ TODO ]
    │   ├── CandleCloseSseHandler.java                                    [ TODO ]
    │   └── IndicatorCloseSseHandler.java                                 [ TODO ]
    └── controller/
        └── StreamController.java                                         [ TODO ]  4개 GET 엔드포인트, handler 위임
```

### Kafka 토픽 / Consumer 그룹

- 토픽명: 기존 컨벤션 그대로 (`{소스모듈}.{데이터타입}`). api 모듈은 publish 안 함, consume only
- group-id: `api.stream.{도메인}.v1` (다른 모듈 그룹과 충돌 금지)
- 다중 인스턴스 시: 모든 인스턴스가 모든 메시지 수신해야 fanout이 맞음 → **인스턴스마다 group-id 고유화** 또는 Redis Pub/Sub 브릿지로 전환

### Sink 정책

```java
Sinks.Many<Tick> tickSink = Sinks.many().multicast().onBackpressureBuffer();
// SSE 핸들러: tickSink.asFlux().filter(t -> matches(criteria)).subscribe(emitter::send)
```

- multicast (한 메시지 → N 구독자)
- backpressure buffer (느린 클라이언트 보호, 임계 초과 시 emitter 종료)
- emitter 타임아웃: 30분, 클라이언트 자동 재연결에 의존

---

## Kafka 토픽 / Redis 키 컨벤션 준수

| 항목 | 참조 |
|------|------|
| Kafka 토픽 | api 모듈은 토픽 신설 없음 (consume only) |
| Redis 키 | 향후 응답 캐싱 도입 시 `RedisKeys` 유틸 사용. 직접 문자열 조립 금지 |

---

## 다음 시작 지점

세션 복원용. 작업 완료 시 업데이트.

| 항목 | 내용 |
|------|------|
| **현재 작업 모듈** | `api` (단계 5: market 컨트롤러) |
| **첫 번째 시작 파일** | `controller/market/TickController.java` |
| **다음 할 일** | 단계 5 — market_data_query UseCase 10종: TickController, PremiumController, PremiumDetailController, FxController |
| **선결 작업** | market_data_query UseCase 파라미터 확인 필요 |

---

## 검증 포인트 (단계 진입 전 체크리스트)

### skeleton 단계 종료 전
- [ ] `./gradlew :api:bootRun` 정상 기동
- [ ] `GET /actuator/health` 200 응답 (actuator 미포함이면 임시 placeholder 컨트롤러로 대체)
- [ ] `GET /v3/api-docs` 200 응답 (springdoc)
- [ ] dev 프로파일에서 `GET /swagger-ui.html` 접근 가능, prod 프로파일에선 차단
- [ ] `GET /존재하지않는경로` → RFC 7807 `ProblemDetail` JSON 응답 확인
- [ ] `infra_heartbeat`에 `API` 등록 완료, 다른 모듈 헬스체크에서 인식
- [ ] `application.yml`에 시크릿 직접 노출 없음, 모두 `${...}` placeholder
- [ ] `@Autowired` 필드 주입 사용 0건 (전체 생성자 주입)

### 단계 2~5 (각 컨트롤러 단계) 종료 전
- [ ] 모든 컨트롤러 = 도메인별 패키지 분리 (`controller/{domain}/`) 준수
- [ ] 시계열 엔드포인트는 cursor, 메타/리스트는 offset 일관 적용
- [ ] 시간 필드 epoch ms (long), ISO 문자열 0건
- [ ] 응답 DTO는 query 모듈 View record 그대로 반환 (api 자체 DTO 신설 금지)
- [ ] 페이징 응답은 `CursorPage` / `OffsetPage` envelope, 단건/리스트는 raw

### 단계 6 (composition) 종료 전
- [ ] composition 서비스가 query 모듈 UseCase만 호출, 도메인 모듈 직접 호출 0건
- [ ] 합성 응답 record는 `composition/dto/`에만 위치
- [ ] interval/시간 동기화 로직 (캔들+지표) 단위 테스트 보유

### 단계 7 (stream) 종료 전
- [ ] SSE 컨트롤러/핸들러 안에 Kafka 구독 코드 0건 (Sink만 구독)
- [ ] Sink 시그니처가 도메인 객체 (`Sinks.Many<Tick>`), DTO/JSON 아님
- [ ] consumer group-id 다른 모듈과 충돌 없음
- [ ] 다중 인스턴스 시 fanout 정책 명시 (group-id 고유화 or Redis Pub/Sub)
- [ ] emitter 타임아웃·backpressure 정책 적용

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-05-06 | 최초 작성 (6개 결정사항 + 2개 안전장치 + 7단계 작성 순서 확정) |
