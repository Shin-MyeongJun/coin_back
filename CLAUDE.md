# CoinData Platform Project Context

> 2026-05-13 기준으로 실제 Gradle 설정, 모듈 소스, 패키지, Kafka 토픽, 애플리케이션 진입점, 주요 리스크, 프론트엔드 연계 항목을 점검해 갱신한 문서입니다. Claude 및 다른 AI 에이전트는 이 파일을 프로젝트 기준 문서로 먼저 읽으세요.
> 2026-06-12 P1 하드닝: env 판별자 프로퍼티 `app.env` 통일, `:alert` 라이브러리 빌드 정리, alert 도메인 not-found 예외 추가 반영 (§0, §16, §18).
> 이전 갱신: 2026-05-10 (모듈 구조·Kafka·Redis·MVP 제외 항목 1차 정비).

---

## 0. 점검 스냅샷

- 기준 경로: `C:\Users\smj\Desktop\COIN_SERVER\demo`
- 빌드: Java 21, Gradle 8.4, Spring Boot 3.4.5, Spring Cloud 2024.0.1, Spring Modulith 1.3.5
- 확인한 범위: `settings.gradle`, 루트 및 모듈 `build.gradle`, `src/main/java`, `src/test/java`, `application.yml`, 주요 Kafka producer/consumer, Redis key generator, `modules/api/_PLAN.md` 진행 상태, `coin_front/docs/PLAN.md` 인터페이스 합의
- 검증 명령 (2026-06-12 P1 하드닝 시 통과):
  - `.\gradlew.bat compileJava` 성공
  - `.\gradlew.bat compileTestJava` 성공
  - `.\gradlew.bat :alert:test :api:test :user:test` 성공
  - `.\gradlew.bat :alert:assemble :user:assemble :api:assemble` 성공 (`:alert`/`:user`는 bootJar 미생성, `:api`만 bootJar 유지)
- 미실행 범위: 전체 `test`는 Kafka/PostgreSQL/Redis/Testcontainers 의존도가 있어 이번 범위에서는 실행하지 않았습니다.
- Redis 키 패턴(§6) 변경 없음: P1에서는 env 판별자 **프로퍼티 키만** `app.env`로 통일했고, 키 문자열 포맷(`ys:{env}:v1:...`)은 그대로입니다.

---

## 1. 프로젝트 개요

CoinData Platform은 거래소 시세, 환율, 경제지표를 수집하고 김치 프리미엄, 캔들, 기술지표, 조회 API/SSE 스트림을 구성하는 실시간 데이터 파이프라인입니다. 프론트엔드 대시보드는 별도 프로젝트 `coin_front`가 담당하고, 본 저장소는 REST/SSE API 제공까지를 범위로 합니다.

핵심 흐름은 다음과 같습니다.

```text
Exchange WebSocket/API
  -> ingestion-exchange.tick-raw / ingestion-exchange.market-code-raw
  -> meta_data, market_data
  -> market-data.tick / market-data.premium / market-data.premium-detail
  -> analytics
  -> query modules + api REST/SSE
  -> coin_front (별도 프로젝트)
```

주요 인프라는 Kafka, PostgreSQL/TimescaleDB, Redis입니다. Kafka 직렬화는 실제 코드상 JSON record 중심이고, `contracts`에는 Java record와 `.proto`가 함께 존재합니다 (proto는 MVP 미사용).

관측성은 Docker Compose 기반 Prometheus/Grafana/exporter 스택에 더해, 주요 실행 모듈에 Actuator 및 Prometheus registry 설정을 적용합니다. API는 `/actuator/health`, `/actuator/prometheus`를 노출하고, Prometheus local stack은 `host.docker.internal:8080`의 API actuator endpoint를 scrape 대상으로 둡니다.

---

## 2. 실제 Gradle 모듈 구조

`settings.gradle` 기준 실제 모듈은 아래와 같습니다. 디렉터리명 `ingection`은 기존 오타를 유지하고, Java 패키지는 대체로 `ingestion`을 사용합니다.

| Gradle project | Path | Main/Test Java | 역할 및 현재 평가 |
| --- | --- | ---: | --- |
| `:contracts` | `modules/contracts` | 24 / 0 | Kafka 메시지 record와 proto 계약. 실제 런타임 계약은 Java record가 중심입니다. candle/indicator 계열 record는 analytics `Close*` domain 형태를 따라 String payload로 정의되어 있습니다. |
| `:infra_shard` | `modules/infra_shard` | 21 / 0 | 공통 mapper, Kafka consumer base, Redis config/key, JSON/DSL-JSON, SQL loader, exchange connector interface. 안정적인 공통 모듈입니다. |
| `:infra_heartbeat` | `modules/infra_heartbeat` | 32 / 0 | 모듈 heartbeat/health-change Kafka, health cache/status manager. 패키지 오타 `infrastrcuture`가 실제 코드에 남아 있습니다. |
| `:infra_upbit` | `modules/infra_exchange/upbit` | 12 / 0 | Upbit REST/WebSocket client, DTO, 인증 필터, properties. 패키지명은 `infre_exchange.upbit`입니다. |
| `:infra_binance` | `modules/infra_exchange/binance` | 15 / 0 | Binance REST/WebSocket client, futures/spot DTO, properties. 패키지명은 `infre_exchange`입니다. |
| `:ingestion_exchange_shard` | `modules/ingection/exchange/ingestion_exchange_shard` | 23 / 6 | 거래소 수집 공통 계층. raw tick/market-code handler, stream manager, raw publisher, scheduler, in-memory cache를 가집니다. |
| `:upbit_ingestion` | `modules/ingection/exchange/upbit_ingestion` | 10 / 0 | Upbit 전용 수집 앱. shard와 `infra_upbit`을 조합해 구독/파싱/발행을 수행합니다. |
| `:binance_ingestion` | `modules/ingection/exchange/binance_ingestion` | 11 / 0 | Binance 전용 수집 앱. shard와 `infra_binance`를 조합합니다. |
| `:fx_ingestion` | `modules/ingection/fx_ingestion` | 14 / 3 | Naver FX 수집, `ingestion-fx.fx` 발행, 1초 scheduler. application.yml에는 거래소 설정이 과하게 함께 들어 있습니다. |
| `:economic_ind_shard` | `modules/economic/economic_ind/economic_ind_shard` | 41 / 9 | 경제지표 공통 domain/usecase/JPA/cache/scheduler. `SyncScheduleService`에 캐시 일괄 동기화 TODO가 남아 있습니다. |
| `:fred` | `modules/economic/economic_ind/fred` | 13 / 2 | FRED client, parser, scheduled service. 패키지 오타 `clinet`, `parer`가 실제 코드에 남아 있습니다. |
| `:crawling` | `modules/economic/economic_ind/crawling` | 13 / 2 | 별도 crawling 앱. 현재 base package는 `com.example.demo.economic.crawling`으로, FRED/shard의 `com.example.demo.ingestion.economic.economic_ind`와 다릅니다. |
| `:meta_data` | `modules/meta_data` | 34 / 9 | market-code raw 소비, Exchange/MarketCode 저장, `meta-data.exchange`, `meta-data.market-code` 발행, 초기화 runner. |
| `:market_data` | `modules/market_data` | 103 / 37 | tick/fx/meta 소비, cache/buffer, premium 계산, DB/Redis latest 저장, `market-data.*` 발행. 테스트가 가장 촘촘한 편입니다. |
| `:analytics` | `modules/analytics` | 180 / 18 | market-data 이벤트 소비, partition registry, candle/indicator store, Redis state recovery, DB flush scheduler. 패키지는 `analystics` 오타를 유지합니다. |
| `:market_data_query` | `modules/query/market_data_query` | 47 / 0 | market data read side. latest/range/downsample/ranking SQL과 JPA adapter를 제공합니다. |
| `:meta_data_query` | `modules/query/meta_data_query` | 24 / 0 | metadata read side. list/search/integrity query, QueryDSL repository가 있습니다. |
| `:analytics_query` | `modules/query/analytics_query` | 77 / 0 | candle/indicator/screener/downsample/latest read side. SQL + QueryDSL + JPA adapter 조합입니다. |
| `:economic_query` | `modules/query/economic_query` | 32 / 0 | 경제지표 read side. indicator, calendar, correlation 조회 adapter를 제공합니다. |
| `:api` | `modules/api` | 43 / 11 | REST API, composition service, SSE stream, Kafka stream consumer. query 모듈들을 scan하여 조회 API를 구성합니다. `modules/api/_PLAN.md`의 7단계(skeleton → controller × 4 → composition → stream)가 **모두 완료** 상태입니다. |
| `:trading` | `modules/trading` | 0 / 0 | Gradle 모듈과 의존성만 있고 Java 소스는 아직 없습니다. placeholder 상태입니다. |

---

## 3. 의존 방향

큰 방향은 아래처럼 유지합니다.

```text
contracts
  <- infra_shard
  <- infra_exchange/*
  <- ingestion/*
  <- meta_data, market_data, analytics
  <- query/*
  <- api
```

세부 규칙:

- `contracts`는 모든 메시지 record의 기준입니다.
- `infra_shard`는 공통 mapper, Redis, Kafka consumer base, JSON/SQL 유틸만 둡니다.
- `infra_exchange/*`는 거래소 API client/DTO/properties를 담당하고 비즈니스 usecase를 넣지 않습니다.
- `ingestion_exchange_shard`는 거래소 수집 공통 usecase/port/publisher/cache를 담당합니다.
- `upbit_ingestion`, `binance_ingestion`은 거래소별 adapter와 앱 진입점만 담당합니다.
- `economic_ind_shard`는 경제지표 domain/usecase/persistence 공통 모듈이고, `fred`, `crawling`은 공급자별 adapter/app입니다.
- `market_data`, `meta_data`, `analytics`는 write-side 서비스입니다.
- `query/*`는 read-side 모듈입니다. write-side 모듈에 역참조를 만들지 않습니다.
- `api`는 query 모듈과 stream consumer/SSE를 조합하는 진입점입니다. 도메인 모듈 직접 의존 금지 (ArchUnit 가드 권장).
- `trading`은 아직 비어 있으므로 새 기능 추가 전 실제 경계부터 정의해야 합니다.

---

## 4. 주요 데이터 흐름

### Exchange tick/meta

```text
upbit_ingestion / binance_ingestion
  -> ingestion_exchange_shard
  -> Kafka: ingestion-exchange.tick-raw
  -> market_data TickConsumer
  -> ConsumeTickService
  -> Tick cache + TickBuffer + CalPremiumManager
  -> Kafka: market-data.tick, market-data.premium, market-data.premium-detail
```

```text
ingestion_exchange_shard MarketCodeScheduler/Handler
  -> Kafka: ingestion-exchange.market-code-raw
  -> meta_data MarketCodeRawConsumer
  -> Exchange/MarketCode DB save
  -> Kafka: meta-data.exchange, meta-data.market-code
  -> market_data metadata cache
```

### FX

```text
fx_ingestion NaverRealTimeFxClient
  -> NaverRawFxMapper
  -> Kafka: ingestion-fx.fx
  -> market_data FxConsumer
  -> Fx cache
```

### Premium

```text
Tick 수신
  -> CalPremiumManager
  -> 동일 base symbol의 비교 거래소 tick 조회
  -> FX cache 조회
  -> Premium / PremiumDetail 생성
  -> buffer + Redis latest + DB flush + Kafka publish
```

### Analytics

```text
market-data.tick / premium / premium-detail
  -> analytics consumer
  -> key parser
  -> PartitionRegistry
  -> CandleStore / IndicatorStore
  -> AnalyticsDbScheduler interval flush
  -> DB writer
  -> Redis state cache/recovery on partition lifecycle
```

주의: `analytics.*` Kafka publisher 클래스와 토픽 설정은 존재하지만, 현재 flush usecase의 주 경로는 `WriteAnalyticsValuePort`를 통한 DB write입니다. analytics 이벤트 발행까지 기대하는 변경에서는 publisher 연결 여부를 먼저 확인하세요.

### API/read side

```text
api
  -> query modules: REST 조회 (controller × 4 + composition × 3 ✅ DONE)
  -> Kafka stream consumers: market-data.*, analytics.*
  -> SSE handlers (✅ DONE):
       /api/v1/stream/ticks
       /api/v1/stream/premium
       /api/v1/stream/candles/close       (?type=tick|premium)
       /api/v1/stream/indicators/close    (?type=tick|premium)
       /api/v1/stream/alerts              (private JWT account stream)
```

응답 컨벤션 (프론트 합의 기준 — §14 참조):

- 페이징: 시계열 = cursor (`?cursor={epochMs}&limit=`), 메타/리스트 = offset (`?page=&size=`)
- 시간: epoch milliseconds (long), UTC 단일 기준
- 에러: RFC 7807 `ProblemDetail` (`@RestControllerAdvice` 글로벌 핸들러)
- envelope: 단건/리스트 raw, 페이징만 envelope (`{items, nextCursor, hasMore}` / `{items, page, size, total}`)

---

## 5. Kafka 토픽

현재 코드에서 확인되는 주요 토픽:

| Topic | Producer | Consumer |
| --- | --- | --- |
| `ingestion-exchange.tick-raw` | `ingestion_exchange_shard` | `market_data` |
| `ingestion-exchange.market-code-raw` | `ingestion_exchange_shard` | `meta_data` |
| `ingestion-fx.fx` | `fx_ingestion` | `market_data` |
| `economic-ind.indicator` | `economic_ind_shard` | MVP 내 downstream consumer 없음 |
| `meta-data.exchange` | `meta_data` | `market_data` |
| `meta-data.market-code` | `meta_data` | `market_data` |
| `market-data.tick` | `market_data` | `analytics`, `api` SSE |
| `market-data.premium` | `market_data` | `analytics`, `api` SSE |
| `market-data.premium-detail` | `market_data` | `analytics`, `api` SSE |
| `analytics.tick-candle` | `analytics` publisher class | `api` SSE |
| `analytics.premium-candle` | `analytics` publisher class | `api` SSE |
| `analytics.premium-detail-candle` | `analytics` publisher class | `api` SSE |
| `analytics.tick-indicator` | `analytics` publisher class | `api` SSE |
| `analytics.premium-indicator` | `analytics` publisher class | `api` SSE |
| `{moduleName}.heartbeat` | `infra_heartbeat` | `infra_heartbeat` |
| `{moduleName}.health-change` | `infra_heartbeat` | `infra_heartbeat` |

토픽명을 바꿀 때는 producer, consumer, `NewTopic` config, 테스트 assertion, API stream test, coin_front의 SSE 이벤트명까지 함께 갱신해야 합니다.

---

## 6. Redis 키 규칙

공통 prefix는 `ys:{env}:v1`입니다.

`infra_shard.RedisKeys`:

```text
ys:{env}:v1:tick:latest:{marketCodeId}
ys:{env}:v1:premium:latest:{baseEx}:{compareEx}:{symbol}
ys:{env}:v1:premium:detail:latest:{baseEx}:{compareEx}:{symbol}
```

`analytics` state key generator:

```text
ys:{env}:v1:tick:candle:state:{partitionId}:{tf}
ys:{env}:v1:premium:candle:state:{partitionId}:{tf}
ys:{env}:v1:premium:detail:candle:state:{partitionId}:{tf}
ys:{env}:v1:tick:indicator:state:{partitionId}:{tf}
ys:{env}:v1:premium:indicator:state:{partitionId}:{tf}
```

Redis 연결 기본값은 `localhost:6379`이며, `spring.data.redis.host`/`spring.data.redis.port` 또는 `REDIS_HOST`/`REDIS_PORT` 환경변수로 override할 수 있습니다.

---

## 7. 아키텍처와 코딩 컨벤션

대부분의 서비스 모듈은 hexagonal architecture를 따릅니다.

```text
application/
  port/in       UseCase interface
  port/out      outbound port interface
  usecase       usecase implementation

domain/
  domain        record, enum, sealed interface
  service       pure domain service
  buffer/store  in-memory buffer/state store

infrastructure/
  messaging     Kafka consumer/producer/config
  persistence   entity/repo/mapper/adapter
  cache         Redis/in-memory cache
  scheduler     scheduled jobs
```

Generic type naming:

| Type | 의미 |
| --- | --- |
| `DOMAIN` | 도메인 객체 |
| `MESSAGE` | Kafka message record |
| `ENTITY` | JPA entity |
| `KEY` | cache/map/key object |
| `VAL` | value object |
| `RAW` | raw data |

공통 mapper:

```java
MessageToDomain<MESSAGE, DOMAIN>
DomainToMessage<DOMAIN, MESSAGE>
MessageMapping<DOMAIN, MESSAGE>
DomainToEntity<DOMAIN, ENTITY>
EntityToDomain<ENTITY, DOMAIN>
EntityMapping<DOMAIN, ENTITY>
RawToMessage<RAW, MESSAGE>
RawToDomain<RAW, DOMAIN>
```

Spring 규칙:

- DI는 생성자 주입을 기본으로 합니다. Lombok 사용 시 `@RequiredArgsConstructor` + `private final`.
- Port 구현체는 `@Component` 또는 `@Repository`.
- UseCase 구현체는 `@Service` 또는 `@Component`.
- 설정은 `@Configuration` + `@Bean`.
- 불변 도메인은 Java `record`를 우선합니다.
- 상태 계층은 필요한 경우 class를 사용하되 setter를 남발하지 않습니다.

---

## 8. 유지해야 하는 기존 오타와 주의 패키지

이미 코드와 import에 녹아 있는 오타는 별도 rename 작업이 아니면 그대로 맞춥니다.

- 디렉터리: `modules/ingection`
- analytics package: `com.example.demo.analystics`
- infra exchange package: `com.example.demo.infre_exchange`
- heartbeat package: `infrastrcuture`
- exchange shard package 일부: `infrastruct`
- FRED package: `clinet`, `parer`
- analytics cache writer package: `wirter`
- 기타 이름: `FxMessageSchdeuled`, `SaveAndFlushPriseValuePort`, `UpbitMarketCodMapper`

대규모 rename은 10개 이상 파일을 건드리기 쉬우므로 일반 기능 작업에 섞지 않습니다.

---

## 9. 현재 리스크와 정리 과제

1. build.gradle 과잉 의존성: 여러 실행 모듈에 Cassandra, security, template engine, MySQL, Datadog 등 실제 사용 범위를 넘는 dependency가 반복됩니다.
2. `infra_heartbeat/build.gradle`: `implementation implementation(project(...))` 형태가 남아 있습니다. 현재 컴파일은 통과하지만 정리 대상입니다.
3. application.yml 중복과 하드코딩: 주요 실행 모듈의 Kafka bootstrap, 거래소/FRED endpoint, `ddl-auto`는 env fallback 형태로 정리했습니다. 새 설정은 `KAFKA_BOOTSTRAP_SERVERS`/`KAFKA_SERVERS`, `JPA_DDL_AUTO`, 거래소별 `*_BASE_URL`/`*_WS_URL` 환경변수를 우선합니다. 다만 각 모듈에 중복된 거래소 설정 블록 자체는 아직 남아 있어 별도 공통화 후보입니다.
4. contracts 불일치 가능성: Java record와 proto가 공존합니다. Kafka JSON 직렬화 기준은 Java record입니다.
5. analytics event payload: candle/indicator message record는 analytics `Close*` domain 형태를 따라 채워져 있고, flush 시점에 DB write 이후 Kafka publish까지 이어집니다. Kafka publish 실패 처리를 위한 outbox 패턴은 Bundle D로 도입되었습니다 (`AnalyticsOutboxEntity`, `AnalyticsOutboxPublisher`, 스키마는 `modules/analytics/src/main/resources/db/migration/V100__analytics_outbox.sql`). `:analytics` 모듈은 로컬 기본값으로 `ANALYTICS_FLYWAY_ENABLED=false` + `JPA_DDL_AUTO=update`를 유지합니다. 운영형 환경에서는 V100을 Flyway 또는 외부 migration 파이프라인으로 먼저 적용한 뒤 `JPA_DDL_AUTO=validate|none`을 사용하세요. multi-instance 중복 발행은 `AnalyticsOutboxJpaRepository.findPendingForUpdateSkipLocked()`의 `FOR UPDATE SKIP LOCKED`와 `AnalyticsOutboxPublisher.publishPending()` 트랜잭션으로 같은 pending row를 동시에 집지 못하게 막습니다. Kafka 전송 성공 후 DB mark 전 프로세스 종료 같은 경우는 여전히 at-least-once 범위입니다.
6. economic publisher: `EcoIndPublisher.publish()`는 `economic-ind.indicator` Kafka 발행까지 구현되어 있습니다. 다만 MVP 내 downstream consumer/API stream은 아직 없으므로 필요 시 별도 read/stream 통합이 필요합니다.
7. query modules Docker tests: query 4종(`market_data_query`, `meta_data_query`, `analytics_query`, `economic_query`)의 PostgreSQL/Testcontainers 기반 SQL 통합 테스트는 JUnit tag `docker`로 분리했습니다. 기본 `test` 태스크에서는 제외되며, Docker 환경에서 `:market_data_query:dockerTest`처럼 모듈별 `dockerTest`로 실행합니다.
8. trading: 소스가 없는 placeholder입니다.
9. `TickBuffer.flush()` 관련 예전 문서의 버그는 현재 코드에서 해결되어 있습니다. `flush()`가 snapshot 후 `buffer.clear()`를 호출합니다.
10. API security: public GET 조회와 public market/analytics SSE만 `permitAll`입니다. `/api/v1/watchlist/**`, `/api/v1/alert/**`, `/api/v1/api-keys/**`, `/api/v1/auth/me|logout`, `/api/v1/stream/alerts`는 JWT account principal 전용입니다. API key principal은 `/api/v1/auth/sse-ticket` 같은 명시 endpoint에서만 허용하고, account controller가 `AuthenticatedAccount == null` 상태로 실행되지 않게 SecurityConfig에서 차단합니다.
11. Alert evaluator 경로: PremiumAlertBridge 기반 API 내부 sink 구독 경로는 제거했습니다. alert 평가는 Kafka consumer(`market-data.*`, `analytics.*-indicator`)가 `EvaluateMarketSignalUseCase`로 넘기는 단일 경로입니다. `AlertMetric` enum은 현 시점 `BUY_PREMIUM_RATE` / `SELL_PREMIUM_RATE`만 지원 — coin_front 가 가정하는 `LAST_PRICE`/`RSI`/`MACD`/`BOLLINGER_*`와의 갭은 `docs/notes/alert-metric-gap.md` 참고.
12. Flyway 버전 관리: `:user` (V1, V2) 와 `:alert` (V10, V11, V12) 가 모두 `:api` 의존성으로 들어와 `classpath:db/migration` 하나에 합쳐집니다. 버전 충돌을 피하려고 `:alert` 는 V10 이상, `:analytics` outbox 는 V100 이상으로 채번합니다. 새 모듈/마이그레이션 추가 시 같은 채번 규칙을 따르세요.

---

## 10. 검증 명령

문서 갱신 시점에 통과한 기본 검증:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat compileTestJava
```

변경 성격별 권장 검증:

```powershell
.\gradlew.bat :market_data:test
.\gradlew.bat :analytics:test
.\gradlew.bat :api:test
.\gradlew.bat :economic_ind_shard:test
.\gradlew.bat :fred:test
.\gradlew.bat :crawling:test
```

런타임 실행 (`scripts/run/start-runtime.ps1`):

```powershell
.\scripts\run\start-runtime.ps1
.\scripts\run\start-runtime.ps1 -ApiInstances 2 -ApiBasePort 8080
.\scripts\run\start-runtime.ps1 -IncludeIngestion -IncludeEconomic
.\scripts\run\start-runtime.ps1 -All
```

전체 테스트는 Kafka, PostgreSQL, Redis 상태에 영향을 받을 수 있습니다. query 모듈의 PostgreSQL/Testcontainers 통합 테스트는 기본 `test`에서 제외되며 Docker 환경에서 `.\gradlew.bat :market_data_query:dockerTest :meta_data_query:dockerTest :analytics_query:dockerTest :economic_query:dockerTest`처럼 별도로 실행하세요.

---

## 11. 작업 가드레일

다음 명령은 사용자 명시 확인 없이 실행하지 않습니다. 실행 의도가 있으면 dry-run 또는 영향 범위를 먼저 보고합니다.

- `git clean` 모든 형태. 먼저 `git clean -n`.
- `git reset --hard`
- `git checkout -- <path>`
- `git stash drop`, `git stash clear`
- `git branch -D`
- `git push --force`, `git push --force-with-lease`
- `rm -rf`, PowerShell `Remove-Item -Recurse -Force`
- `.\gradlew.bat clean`
- 모든 `DROP`, `TRUNCATE`, `DELETE` SQL

10개 이상 파일에 영향을 주는 mass refactoring 전에는:

1. `git status` 확인
2. dirty/untracked 변경을 사용자에게 보고
3. 별도 브랜치 생성
4. rename과 기능 변경을 분리

---

## 12. AI 에이전트 작업 원칙

- 새 코드는 주변 모듈의 기존 패키지/오타/패턴에 맞춥니다.
- topic, Redis key, DB entity/table, message record를 바꾸면 producer/consumer/query/API/test/coin_front SSE 이벤트명을 같이 추적합니다.
- read-side query 모듈은 write-side 모듈에 의존하지 않게 유지합니다.
- common abstraction은 `infra_shard`에 두되, 특정 도메인 규칙을 무리하게 공통화하지 않습니다.
- 문서가 코드와 다르면 코드를 우선 확인하고 문서를 갱신합니다.
- 한국어 문서는 UTF-8로 저장합니다.

---

## 13. MVP 제외 항목

아래 항목은 현재 CoinData MVP 범위에서 제외합니다. 구현 흔적이나 계약 파일이 남아 있더라도, 면접/README/문서에서는 "미완성 기능"이 아니라 "이번 MVP에서 의도적으로 제외한 확장 후보"로 설명합니다.

### 13.1 Trading module

- `:trading` 모듈은 향후 자동매매/전략 실행/주문 연동을 위한 확장 후보입니다.
- 현재 MVP는 실시간 시세 수집, 김치 프리미엄 계산, 캔들/지표 분석, 조회 API/SSE 제공까지를 범위로 삼습니다.
- 주문 실행, 포지션 관리, 거래소 private API 연동, 리스크 관리, 백테스팅은 MVP 범위에 포함하지 않습니다.
- 따라서 `:trading`에 구현 소스가 없거나 placeholder 상태인 것은 현재 MVP의 기능 누락으로 보지 않습니다. README 작성 시에도 "Future scope" 또는 "Out of MVP scope"로 명시합니다.

### 13.2 Contracts proto files

- 현재 런타임 Kafka 메시지 계약은 Java record + JSON 직렬화를 기준으로 합니다.
- `contracts` 모듈에 남아 있는 `.proto` 파일은 향후 gRPC, schema registry, binary serialization, cross-language client 지원을 검토하기 위한 확장 후보입니다.
- MVP에서는 proto 기반 직렬화나 gRPC 계약 생성을 사용하지 않습니다.
- README 작성 시에는 "현재 MVP는 Java record 기반 Kafka JSON 계약을 사용하며, proto는 향후 계약 안정화/다언어 연동을 위한 후보"라고 설명합니다.

### 13.3 Economic realtime downstream

- 경제지표 수집, 저장, 조회 계층은 프로젝트 안에 존재합니다.
- 다만 현재 MVP의 핵심 검증 범위는 거래소 tick, 환율, premium, candle, indicator로 이어지는 market data pipeline입니다.
- 경제지표를 `analytics` 실시간 계산이나 API SSE stream으로 연결하는 downstream 통합은 MVP 범위에서 제외합니다.
- README/API 문서에서는 경제지표 REST 조회는 제공하되, 실시간 분석 이벤트 파이프라인의 핵심 데모 범위는 아니라고 명시합니다.

### 13.4 Frontend dashboard

- CoinData 백엔드 MVP는 REST/SSE API 제공까지를 범위로 삼습니다.
- 실시간 차트, dashboard, 사용자 화면은 별도 프론트엔드 프로젝트 `coin_front`에서 담당합니다.
- 따라서 이 저장소에 화면이 없는 것은 현재 MVP의 결함으로 보지 않습니다.
- README에서는 "프론트엔드 대시보드는 별도 프로젝트에서 담당"한다고 명시합니다.

### 13.5 사용자 기능 백엔드 (인증/알람/워치리스트/API Key)

- coin_front의 화면(로그인, 알람 규칙, 관심 목록, API Key 관리)에 대응하는 백엔드 모듈은 본 MVP에 포함되지 않습니다.
- 프론트는 인터페이스 placeholder(`features/*/api/*.ts`)만 갖춘 상태로 진행하며, 백엔드 모듈 추가 후 어댑터 레이어만 교체하는 구조입니다.
- 향후 작업 항목과 합의 사항은 §14를 참조하세요.

---

## 14. 프론트엔드(coin_front) 연계

coin_front는 Next.js 15 / Vite + React 19, TailwindCSS + shadcn/ui, lightweight-charts v4, TanStack Query v5, EventSource(SSE), Zustand, react-hook-form + zod 스택으로 본 백엔드의 REST/SSE를 소비합니다. 자세한 화면 계획과 백엔드 합의 항목은 coin_front 저장소의 `docs/PLAN.md`(특히 §14 "백엔드와의 합의 필요 사항")와 `docs/FRONTEND_GUIDE.md`를 참조합니다.

### 14.1 합의된 응답 컨벤션

| 항목 | 결정 |
|------|------|
| 페이징 | 시계열 = cursor (`?cursor={epochMs}&limit=`), 메타/리스트 = offset (`?page=&size=`) |
| 시간 | epoch milliseconds (long), UTC 단일 기준 |
| 에러 | RFC 7807 `ProblemDetail` |
| envelope | 단건/리스트 raw, 페이징만 envelope (`{items, nextCursor, hasMore}` / `{items, page, size, total}`) |
| 토픽 / SSE 이벤트명 | 백엔드 컨벤션 그대로 사용 (`tick`, `premium`, `tick-candle`, `premium-candle`, `tick-indicator`, `premium-indicator`) |

### 14.2 프론트가 사용하는 백엔드 엔드포인트 (현재 가용)

- `/api/v1/meta/*` — `meta_data_query` 4 UseCase
- `/api/v1/market/*` — `market_data_query` 10 UseCase
- `/api/v1/analytics/*` — `analytics_query` 9 UseCase (캔들·지표·screener·downsample·latest)
- `/api/v1/economic/*` — `economic_query` 6 UseCase
- `/api/v1/compose/*` — 합성 엔드포인트 3종 (market-overview, chart, dashboard)
- `/api/v1/stream/*` — public SSE 4종 (ticks, premium, candles/close, indicators/close)
- `/api/v1/watchlist/*` — `:alert` watchlist (add / remove / search) (백엔드 구현 완료)
- `/api/v1/alert/rules`, `/api/v1/alert/firings`, `/api/v1/stream/alerts` — `:alert` 모듈 (JWT account 전용 private endpoint)

전체 매핑은 `modules/api/_PLAN.md`를 정본으로 합니다.

### 14.3 백엔드 작업 대기 항목 (MVP 이후 후보)

프론트는 인터페이스만 정의하고 백엔드 모듈 추가 대기 중입니다.

| # | 영역 | 프론트 가정 | 백엔드 작업 필요 |
|---|------|-------------|------------------|
| 1 | 인증 | JWT (access in-memory + refresh httpOnly cookie) | `/api/v1/auth/{login,signup,refresh,me}`, 인증 필터 |
| 2 | API Key | label / scopes / IP whitelist / cooldown / usage | `/api/v1/api-keys/*`, scope 모델, usage 집계 |
| 3 | 권한 스코프 | 6개 1차안 (coin_front §7) | scope enum 합의 |
| 4 | 알람 규칙 | threshold / cooldown / 채널 / label | `/api/v1/alert/{rules,firings}`, 규칙 평가 엔진 (백엔드 구현 완료 — Bundle B) |
| 5 | 워치리스트 | 로그인 전 localStorage → 서버 머지 | `/api/v1/watchlist/*` (백엔드 구현 완료 — Bundle A) |
| 6 | SSE 인증 | URL 쿼리 토큰 (EventSource 헤더 제약) | 토큰 검증 + UUID consumer group fanout 보강 |
| 7 | CORS prod | 화이트리스트 | 현재 dev 와일드카드 → prod 화이트리스트 전환 |
| 8 | OpenAPI 노출 | dev 프로파일에서만 | 현행 유지, prod 차단 검증 |

위 항목은 본 저장소의 향후 작업(별도 모듈 또는 `api` 모듈 확장)으로 진입할 때 §14가 source가 됩니다. coin_front 측은 어댑터 레이어(`lib/api/*`)만 수정해서 mock → real 전환을 처리합니다.

### 14.4 외부 의존 (백엔드 미경유)

coin_front 상단 글로벌 인디케이터 바는 일부 외부 API를 직접 호출합니다. 백엔드는 관여하지 않으나, 향후 운영 단계에서 캐시/프록시 후보로 검토할 수 있습니다.

- alternative.me Fear & Greed Index
- 금/은 시세
- 나스닥 지수
- 미국 10년물 국채 금리

---

## 15. 문서 갱신 절차

`CLAUDE.md`는 source of truth입니다. 본 문서를 갱신할 때는:

1. 점검 스냅샷(§0) 날짜와 검증 명령 통과 여부 갱신
2. 모듈 표(§2) Main/Test Java 개수가 크게 달라졌으면 갱신
3. Kafka 토픽(§5) / Redis 키(§6) 추가·변경 시 producer·consumer·테스트·coin_front까지 cross-reference
4. 패키지 오타 목록(§8) 신규 발견 시 추가
5. MVP 제외(§13) / 프론트 연계(§14) 항목 변경 시 `02_PROJECT_CONTEXT.md`도 함께 갱신
6. 본 문서 갱신 후 `02_PROJECT_CONTEXT.md` §검증 포인트 cross-check 통과 확인

---

## 16. Alert module update (2026-05-19)

- `:alert` 는 **`:api` 가 import 하는 라이브러리 모듈**입니다 (`:api` build.gradle 의 `implementation project(':alert')`). 별도 Spring Boot 런타임을 띄우지 않으므로 `scripts/run/start-runtime.ps1` 의 module 목록에서도 제외되어 있습니다.
- 모듈 root package 는 `com.example.demo.alert`, 의존성은 `:contracts`, `:infra_shard`, `:infra_heartbeat`, `:user` (인증 컨텍스트). `:market_data` / `:analytics` 직접 의존은 금지입니다.
- Kafka consume: `market-data.tick`, `market-data.premium`, `market-data.premium-detail`, `analytics.tick-indicator`, `analytics.premium-indicator`.
- Kafka publish: `alert.firing` (`com.example.demo.contracts.message.alert.AlertFiringMessage`).
- Redis cooldown key: `RedisKeys.alertCooldown(env, ruleId)` → `ys:{env}:v1:alert:cooldown:{ruleId}`.
- REST endpoints 는 `:api` 런타임에서 노출됩니다: `/api/v1/alert/rules`, `/api/v1/alert/firings`, `/api/v1/stream/alerts`, `/api/v1/watchlist/*`. 이 endpoint들은 JWT account principal 전용입니다.
- Flyway 마이그레이션: `modules/alert/src/main/resources/db/migration/V10__alert_rule.sql`, `V11__alert_firing.sql`, `V12__watchlist_item.sql`. V1~V9 는 `:user` (V1/V2) 와 충돌 회피를 위해 비워 두었습니다.
- 빌드 (2026-06-12): `:alert/build.gradle` 은 `org.springframework.boot` 플러그인을 적용하지 않고 `id 'java-library'` 만 둡니다 (`:user`, `:contracts`, `:infra_shard` 와 동일). main class 가 없으므로 bootJar 는 비활성이며, dependency-management/BOM 은 루트 `subprojects` 가 공급합니다. 컨트롤러의 unnamed `@PathVariable`/`@RequestParam` 바인딩을 위해 `-parameters` 컴파일 플래그는 루트 `subprojects { tasks.withType(JavaCompile) }` 에서 전 모듈 공통으로 부여합니다.
- 예외 매핑 (2026-06-12): not-found 는 도메인 예외 `com.example.demo.alert.domain.exception.AlertRuleNotFoundException`, `com.example.demo.alert.watchlist.domain.exception.WatchlistItemNotFoundException` 를 사용합니다. `infrastructure/web/exception/AlertExceptionHandler` (RFC 7807 `ProblemDetail`, `@RestControllerAdvice(basePackages="com.example.demo.alert.infrastructure.web")`) 가 두 예외를 404 로 매핑합니다. `findByIdForUser` empty 는 "미존재"와 "타 사용자 소유"를 구분하지 않고 모두 404 (존재 비노출, 403 미사용). 기존 `java.util.NoSuchElementException` 매핑은 도메인 예외로 대체되어 제거되었습니다.

---

## 17. Benchmarks module update (2026-05-26)

- `:benchmarks` 는 JSON 파서 교체 결정 근거를 만들기 위한 JMH 전용 모듈입니다. 도메인/애플리케이션 런타임 모듈이 아니므로 Hexagonal Architecture 경계를 적용하지 않고, `modules/benchmarks/src/jmh/java` 아래 plain JVM benchmark 로 격리합니다.
- 의존성은 `:contracts`, `:infra_shard`, `:infra_upbit`, `:infra_binance` 를 기준으로 하며, Jackson/ObjectMapper, Jsoniter(`JsonUtil.fromJson`), DSL-JSON(`DslJsonParserManager.parse`) 경로를 같은 fixture 로 비교합니다. 거래소 raw 파싱 벤치는 실제 DSL-JSON 등록 대상인 `UpbitOrderbookDto` 와 `BinanceStreamFormat<BinanceBookTickerDto>` 를 사용합니다.
- Kafka 토픽 신설과 Redis key 사용은 없습니다.
- 결과 파일은 `modules/benchmarks/build/reports/jmh/results.json`, 포트폴리오 요약 문서는 `docs/portfolio/json-parser-benchmark.md` 입니다. 요약 생성은 `.\gradlew.bat :benchmarks:jmhSummary` 를 사용합니다.
- sanity 실행은 `.\gradlew.bat :benchmarks:jmh -Pjmh.iterations=2 -Pjmh.fork=1` 로 수행할 수 있고, 정식 수치는 사용자가 수동 실행한 결과를 사용합니다.

---

## 18. P1 하드닝 (2026-06-12)

세 개의 독립 패키지를 패키지별 커밋으로 적용했습니다.

- **env 판별자 통일 (A)**: Redis 네임스페이스 env 를 읽는 `RateLimitFilter`, `RedisSseTicketStoreAdapter` 의 `@Value` 를 `${ys.env:${YS_ENV:local}}` → `${app.env:local}` 로 바꿔 나머지 모듈(`market_data`, `analytics`, `user`)과 동일한 `app.env` 단일 키로 통일했습니다. `:api` 테스트 프로퍼티도 `app.env=test` 로 변경. **Redis 키 문자열 포맷(§6)은 불변** — 프로퍼티 키만 통일했습니다. `ys.auth.*`, `ys.security.*` 설정 네임스페이스는 env 판별자가 아니므로 그대로 둡니다.
- **alert 도메인 예외 + RFC7807 (B)**: §16 "예외 매핑" 참조. 단위 테스트 `AlertRuleCommandServiceTest`, 슬라이스 테스트 `AlertRuleControllerErrorTest`(+ slice anchor `AlertTestApplication`) 추가.
- **alert 라이브러리 빌드 정리 (C)**: §16 "빌드" 참조. `:alert` boot 플러그인 제거 + 루트 `-parameters` 공통 부여.
- 검증: `:alert:test :api:test :user:test`, `:alert:assemble :user:assemble :api:assemble`, `compileJava`/`compileTestJava` 모두 통과.
- 미적용: 핸드오프가 참조한 `02_PROJECT_CONTEXT.md` 는 저장소에 존재하지 않아 동기화 대상에서 제외했습니다. `:alert` build.gradle 의존성 다이어트(`spring-boot-starter-security`, `org.postgresql:postgresql` runtimeOnly 전환 등)는 "한 번에 한 가지" 원칙에 따라 별도 과제로 남깁니다 (§9-1).
