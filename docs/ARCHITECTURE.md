# CoinData Architecture

이 문서는 CoinData의 백엔드 MVP 구조를 설명합니다. 더 자세한 모듈별 source of truth는 루트의 `CLAUDE.md`입니다.

## Goal

CoinData의 핵심 목표는 실시간 market data pipeline을 끝까지 닫는 것입니다.

```text
raw market data
  -> normalized market data
  -> premium analytics
  -> candle/indicator analytics
  -> query API
  -> realtime SSE
```

여기서 중요한 점은 단순히 Kafka를 사용하는 것이 아니라, 각 단계가 독립 모듈로 나뉘고 Kafka topic, DB table, Redis key, API/SSE가 일관된 계약으로 이어진다는 점입니다.

## High-Level Flow

```text
Upbit / Binance / FX provider
  |
  v
ingestion modules
  |
  | Kafka
  v
meta_data + market_data
  |
  | Kafka + PostgreSQL + Redis
  v
analytics
  |
  | Kafka + PostgreSQL + Redis state
  v
query modules
  |
  v
api REST + SSE
```

## Module Responsibilities

| Module | Responsibility |
| --- | --- |
| `contracts` | Kafka message record. 현재 런타임은 Java record + JSON 직렬화 기준입니다. |
| `infra_shard` | 공통 mapper, Kafka consumer base, Redis config/key, JSON/SQL utility. |
| `infra_heartbeat` | 모듈 heartbeat와 health-change event. |
| `infra_exchange/upbit` | Upbit REST/WebSocket client와 DTO. |
| `infra_exchange/binance` | Binance REST/WebSocket client와 DTO. |
| `ingection/exchange/ingestion_exchange_shard` | 거래소 수집 공통 usecase, handler, scheduler, raw publisher. |
| `upbit_ingestion` | Upbit 전용 수집 application. |
| `binance_ingestion` | Binance 전용 수집 application. |
| `fx_ingestion` | FX 수집 application. |
| `meta_data` | market-code raw를 DB에 저장하고 `meta-data.*` topic으로 발행. |
| `market_data` | tick/fx/meta 소비, tick 저장, premium/premium-detail 계산, Redis latest 저장, Kafka 발행. |
| `analytics` | market-data event 기반 candle/indicator state update, DB flush, Redis state recovery, Kafka event 발행. |
| `query/*` | write-side와 분리된 read-side 조회 모듈. |
| `api` | REST controller, query composition, Kafka stream consumer, SSE fanout. |
| `trading` | MVP 제외. 향후 자동매매/주문 연동 확장 후보. |

## Hexagonal Architecture

주요 서비스 모듈은 다음 구조를 유지합니다.

```text
application/
  port/in       inbound usecase interface
  port/out      outbound persistence/messaging/cache port
  usecase       application service

domain/
  domain        record, enum, value object
  service       domain calculation
  buffer/store  in-memory state

infrastructure/
  messaging     Kafka consumer/producer/config
  persistence   JPA entity/repository/mapper/adapter
  cache         Redis or memory cache
  scheduler     periodic flush/sync
```

이 구조를 쓰는 이유는 다음과 같습니다.

- write-side와 read-side를 분리해서 `query/*`가 processing 모듈 내부 구현에 묶이지 않게 합니다.
- Kafka, Redis, PostgreSQL 같은 외부 기술을 domain/usecase 바깥으로 밀어냅니다.
- message record, entity, domain object를 명시적으로 변환해서 topic/table 변경 영향을 추적하기 쉽게 합니다.

## Kafka Topics

| Topic | Producer | Consumer |
| --- | --- | --- |
| `ingestion-exchange.tick-raw` | ingestion exchange shard | `market_data` |
| `ingestion-exchange.market-code-raw` | ingestion exchange shard | `meta_data` |
| `ingestion-fx.fx` | `fx_ingestion` | `market_data` |
| `meta-data.exchange` | `meta_data` | `market_data` |
| `meta-data.market-code` | `meta_data` | `market_data` |
| `market-data.tick` | `market_data` | `analytics`, `api` |
| `market-data.premium` | `market_data` | `analytics`, `api` |
| `market-data.premium-detail` | `market_data` | `analytics`, `api` |
| `analytics.tick-candle` | `analytics` | `api` |
| `analytics.premium-candle` | `analytics` | `api` |
| `analytics.premium-detail-candle` | `analytics` | `api` |
| `analytics.tick-indicator` | `analytics` | `api` |
| `analytics.premium-indicator` | `analytics` | `api` |
| `{moduleName}.heartbeat` | heartbeat module | heartbeat module |
| `{moduleName}.health-change` | heartbeat module | heartbeat module |

Topic 이름을 바꿀 때는 producer, consumer, `NewTopic`, E2E script, API stream test를 함께 갱신해야 합니다.

## Market Data Flow

### Market Code

```text
upbit_ingestion / binance_ingestion
  -> ingestion-exchange.market-code-raw
  -> meta_data
  -> exchange / market_code table
  -> meta-data.exchange / meta-data.market-code
  -> market_data metadata cache
```

### Tick

```text
upbit_ingestion / binance_ingestion
  -> ingestion-exchange.tick-raw
  -> market_data TickConsumer
  -> Tick cache + TickBuffer
  -> tick table
  -> Redis latest tick
  -> market-data.tick
```

### Premium

```text
tick received
  -> CalPremiumManager
  -> matching exchange/base symbol lookup
  -> FX cache lookup
  -> Premium / PremiumDetail
  -> premium / premium_detail table
  -> Redis latest premium/detail
  -> market-data.premium / market-data.premium-detail
```

Premium 계산은 tick 입력 순서와 FX 방향의 영향을 받을 수 있습니다. 테스트에서는 비교할 양쪽 거래소 tick이 cache에 들어간 뒤, 의도한 base/compare 방향이 계산되도록 마지막 tick을 한 번 더 넣는 방식으로 안정화할 수 있습니다.

## Analytics Flow

```text
market-data.tick / premium / premium-detail
  -> analytics Kafka consumer
  -> PartitionRegistry
  -> CandleStore / IndicatorStore
  -> scheduled flush
  -> tick_candle / premium_candle / premium_detail_candle
  -> tick_indicator / premium_indicator
  -> analytics.* Kafka topic
  -> API SSE
```

`analytics`는 Kafka partition lifecycle에 맞춰 store를 준비하고, Redis에 state를 저장/복원합니다. 이 구조 덕분에 consumer rebalance 이후에도 candle/indicator 계산 상태를 복구할 수 있습니다.

## Redis Keys

공통 prefix는 `ys:{env}:v1`입니다.

```text
ys:{env}:v1:tick:latest:{marketCodeId}
ys:{env}:v1:premium:latest:{baseEx}:{compareEx}:{symbol}
ys:{env}:v1:premium:detail:latest:{baseEx}:{compareEx}:{symbol}
```

Analytics state:

```text
ys:{env}:v1:tick:candle:state:{partitionId}:{tf}
ys:{env}:v1:premium:candle:state:{partitionId}:{tf}
ys:{env}:v1:premium:detail:candle:state:{partitionId}:{tf}
ys:{env}:v1:tick:indicator:state:{partitionId}:{tf}
ys:{env}:v1:premium:indicator:state:{partitionId}:{tf}
```

## Data Stores

주요 PostgreSQL tables:

| Area | Tables |
| --- | --- |
| Metadata | `exchange`, `market_code` |
| Market data | `tick`, `premium`, `premium_detail` |
| Analytics candle | `tick_candle`, `premium_candle`, `premium_detail_candle` |
| Analytics indicator | `tick_indicator`, `premium_indicator` |
| Economic | economic indicator metadata/series 계열 |

TimescaleDB hypertable 구성은 Docker DB 초기화와 JPA schema update 결과에 의존합니다. 로컬 MVP 검증에서는 `JPA_DDL_AUTO=update`를 사용할 수 있고, 운영형 환경에서는 `validate` 또는 migration 기반 전환이 필요합니다.

## API Layer

`api`는 두 경로를 제공합니다.

```text
REST
  -> query modules
  -> PostgreSQL read

SSE
  -> Kafka stream consumer
  -> Reactor Sinks.Many
  -> SseEmitter
```

SSE endpoint는 연결 직후 `connected` event를 한 번 보냅니다. 클라이언트는 이 event를 연결 확인용으로만 처리하고 실제 데이터 event는 `tick`, `premium`, `premium-detail`, `tick-candle`, `premium-candle`, `premium-detail-candle`, `tick-indicator`, `premium-indicator`를 보면 됩니다.

## MVP Boundaries

MVP에 포함하는 것은 market data pipeline입니다.

포함:

- exchange raw market-code/tick ingestion
- FX ingestion
- tick/premium/premium-detail processing
- candle/indicator analytics
- REST query
- SSE realtime stream
- local Docker observability

제외:

- `trading` 주문/전략/포지션/리스크 관리
- proto/gRPC/schema registry 기반 계약 운영
- 경제지표의 실시간 downstream 분석/SSE 통합
- 프론트엔드 대시보드

제외 항목은 구현 실패가 아니라 범위 관리입니다. 문서와 면접에서는 “market data MVP에 집중하고, trading/economic realtime/proto는 확장 후보로 분리했다”라고 설명합니다.
