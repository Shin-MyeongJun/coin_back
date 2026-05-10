# CoinData Platform Project Context

> 2026-05-10 기준으로 실제 Gradle 설정, 모듈 소스, 패키지, Kafka 토픽, 애플리케이션 진입점, 주요 리스크를 점검해 갱신한 문서입니다. Claude 및 다른 AI 에이전트는 이 파일을 프로젝트 기준 문서로 먼저 읽으세요.

---

## 0. 점검 스냅샷

- 기준 경로: `C:\Users\smj\Desktop\COIN_SERVER\demo`
- 빌드: Java 21, Gradle 8.4, Spring Boot 3.4.5, Spring Cloud 2024.0.1
- 확인한 범위: `settings.gradle`, 루트 및 모듈 `build.gradle`, `src/main/java`, `src/test/java`, `application.yml`, 주요 Kafka producer/consumer, Redis key generator
- 검증 명령:
  - `.\gradlew.bat compileJava` 성공
  - `.\gradlew.bat compileTestJava` 성공
- 미실행 범위: 전체 `test`는 Kafka/PostgreSQL/Redis/Testcontainers 의존도가 있어 이번 문서 갱신 범위에서는 실행하지 않았습니다.

---

## 1. 프로젝트 개요

CoinData Platform은 거래소 시세, 환율, 경제지표를 수집하고 김치 프리미엄, 캔들, 기술지표, 조회 API/SSE 스트림을 구성하는 실시간 데이터 파이프라인입니다.

핵심 흐름은 다음과 같습니다.

```text
Exchange WebSocket/API
  -> ingestion-exchange.tick-raw / ingestion-exchange.market-code-raw
  -> meta_data, market_data
  -> market-data.tick / market-data.premium / market-data.premium-detail
  -> analytics
  -> query modules + api REST/SSE
```

주요 인프라는 Kafka, PostgreSQL/TimescaleDB, Redis입니다. Kafka 직렬화는 실제 코드상 JSON record 중심이고, `contracts`에는 Java record와 `.proto`가 함께 존재합니다.

---

## 2. 실제 Gradle 모듈 구조

`settings.gradle` 기준 실제 모듈은 아래와 같습니다. 디렉터리명 `ingection`은 기존 오타를 유지하고, Java 패키지는 대체로 `ingestion`을 사용합니다.

| Gradle project | Path | Main/Test Java | 역할 및 현재 평가 |
| --- | --- | ---: | --- |
| `:contracts` | `modules/contracts` | 24 / 0 | Kafka 메시지 record와 proto 계약. 실제 런타임 계약은 Java record가 중심입니다. candle/indicator 계열 record 일부는 아직 빈 placeholder입니다. |
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
| `:api` | `modules/api` | 43 / 11 | REST API, composition service, SSE stream, Kafka stream consumer. query 모듈들을 scan하여 조회 API를 구성합니다. |
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
- `api`는 query 모듈과 stream consumer/SSE를 조합하는 진입점입니다.
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
  -> query modules: REST 조회
  -> Kafka stream consumers: market-data.*, analytics.*
  -> SSE handlers: tick, premium, candle close, indicator close
```

---

## 5. Kafka 토픽

현재 코드에서 확인되는 주요 토픽:

| Topic | Producer | Consumer |
| --- | --- | --- |
| `ingestion-exchange.tick-raw` | `ingestion_exchange_shard` | `market_data` |
| `ingestion-exchange.market-code-raw` | `ingestion_exchange_shard` | `meta_data` |
| `ingestion-fx.fx` | `fx_ingestion` | `market_data` |
| `meta-data.exchange` | `meta_data` | `market_data` |
| `meta-data.market-code` | `meta_data` | `market_data` |
| `market-data.tick` | `market_data` | `analytics`, `api` SSE |
| `market-data.premium` | `market_data` | `analytics`, `api` SSE |
| `market-data.premium-detail` | `market_data` | `analytics` |
| `analytics.tick-candle` | `analytics` publisher class | `api` SSE |
| `analytics.premium-candle` | `analytics` publisher class | `api` SSE |
| `analytics.premium-detail-candle` | `analytics` publisher class | currently no API consumer found |
| `analytics.tick-indicator` | `analytics` publisher class | `api` SSE |
| `analytics.premium-indicator` | `analytics` publisher class | `api` SSE |
| `{moduleName}.heartbeat` | `infra_heartbeat` | `infra_heartbeat` |
| `{moduleName}.health-change` | `infra_heartbeat` | `infra_heartbeat` |

토픽명을 바꿀 때는 producer, consumer, `NewTopic` config, 테스트 assertion을 함께 갱신해야 합니다.

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
3. application.yml 중복과 하드코딩: 여러 모듈이 `localhost:9092`, 거래소 endpoint 전체 덩어리, `ddl-auto: update`를 직접 포함합니다. `api`, `crawling` 일부는 env fallback을 사용하지만 일관적이지 않습니다.
4. contracts 불일치 가능성: Java record와 proto가 공존합니다. Kafka JSON 직렬화 기준은 Java record입니다.
5. analytics event payload: candle/indicator message record 일부가 비어 있고 publisher 연결 경로가 DB flush와 분리되어 있습니다. SSE/이벤트 발행 기능을 손볼 때 우선 확인하세요.
6. economic publisher: `EcoIndPublisher.publish()`는 현재 빈 구현입니다. 경제지표 Kafka 발행은 완성 상태로 보지 않습니다.
7. query modules test gap: query 모듈은 main 소스는 있으나 test 소스가 거의 없습니다. 조회 SQL이나 mapper 변경 시 targeted test를 추가하는 편이 안전합니다.
8. trading: 소스가 없는 placeholder입니다.
9. `TickBuffer.flush()` 관련 예전 문서의 버그는 현재 코드에서 해결되어 있습니다. `flush()`가 snapshot 후 `buffer.clear()`를 호출합니다.

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

전체 테스트는 Testcontainers, Kafka, PostgreSQL, Redis 상태에 영향을 받을 수 있습니다. 실패 시 먼저 실패한 모듈과 외부 의존성을 분리해서 확인하세요.

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
- topic, Redis key, DB entity/table, message record를 바꾸면 producer/consumer/query/API/test를 같이 추적합니다.
- read-side query 모듈은 write-side 모듈에 의존하지 않게 유지합니다.
- common abstraction은 `infra_shard`에 두되, 특정 도메인 규칙을 무리하게 공통화하지 않습니다.
- 문서가 코드와 다르면 코드를 우선 확인하고 문서를 갱신합니다.
- 한국어 문서는 UTF-8로 저장합니다.
