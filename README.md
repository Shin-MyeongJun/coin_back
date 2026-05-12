# CoinData

CoinData는 거래소 tick, 환율, 메타데이터를 Kafka로 수집하고 PostgreSQL/Redis에 저장한 뒤, 김치 프리미엄, premium-detail, candle, indicator를 계산해서 REST API와 SSE로 제공하는 실시간 market data pipeline입니다.

이 저장소의 기준 문서는 [CLAUDE.md](CLAUDE.md)입니다. 모듈 구조, Kafka 토픽, Redis key, 기존 패키지 오타, MVP 제외 항목은 해당 문서를 source of truth로 봅니다.

## What It Proves

- Kafka 기반 ingestion -> processing -> analytics -> API 흐름을 실제 모듈로 분리했습니다.
- `market_data`, `analytics`, `query`, `api`가 write/read/stream 책임을 나누며 hexagonal architecture를 유지합니다.
- PostgreSQL/TimescaleDB, Redis latest/state cache, Kafka topic, SSE stream까지 로컬 Docker 환경에서 검증할 수 있습니다.
- `scripts/e2e/stream-smoke.ps1`로 API SSE와 Kafka publish/consume 경로를 반복 검증할 수 있습니다.

## Architecture

```text
Exchange WebSocket/API
  -> ingestion_exchange_shard
  -> Kafka: ingestion-exchange.tick-raw / ingestion-exchange.market-code-raw
  -> meta_data + market_data
  -> Kafka: market-data.tick / market-data.premium / market-data.premium-detail
  -> analytics
  -> Kafka: analytics.*-candle / analytics.*-indicator
  -> api REST + SSE
```

자세한 구조는 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)를 보세요.

## Main Modules

| Module | Role |
| --- | --- |
| `contracts` | Kafka JSON message record와 향후 proto 계약 후보 |
| `infra_shard` | 공통 mapper, Kafka/Redis/JSON/SQL utility |
| `ingection/exchange/*` | Upbit/Binance 수집 app 및 공통 shard |
| `fx_ingestion` | FX 수집 및 `ingestion-fx.fx` 발행 |
| `meta_data` | market-code raw 소비, exchange/market-code 저장 및 발행 |
| `market_data` | tick/fx/meta 소비, tick 저장, premium/premium-detail 합성 |
| `analytics` | tick/premium 기반 candle/indicator state, DB flush, Kafka event 발행 |
| `query/*` | read-side REST 조회용 persistence/query adapter |
| `api` | REST API, Kafka stream consumer, SSE fanout |
| `trading` | MVP 제외. 향후 자동매매/주문 연동 확장 후보 |

## Tech Stack

- Java 21, Spring Boot 3.4.5, Gradle 8.4
- Kafka, PostgreSQL/TimescaleDB, Redis
- Spring Kafka, Spring Data JPA, QueryDSL
- Reactor Sink + Spring `SseEmitter`
- Testcontainers, WireMock, AssertJ, BDDMockito, StepVerifier, ArchUnit
- Prometheus, Grafana, kafka-exporter, redis-exporter

## Quick Start

Docker 인프라를 먼저 실행합니다.

```powershell
cd docker
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
cd ..
```

로컬 실행 시 공통 환경변수를 맞춥니다.

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/coin_data?reWriteBatchedInserts=true"
$env:DB_USER="db_manager"
$env:DB_PASSWORD="10200411"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:UPBIT_OPEN_API_ACCESS_KEY="dummy"
$env:UPBIT_OPEN_API_SECRET_KEY="dummy"
$env:BINANCE_OPEN_API_ACCESS_KEY="dummy"
$env:BINANCE_OPEN_API_SECRET_KEY="dummy"
```

핵심 모듈은 별도 터미널에서 실행합니다.

```powershell
.\gradlew.bat :meta_data:bootRun
.\gradlew.bat :market_data:bootRun
.\gradlew.bat :analytics:bootRun
.\gradlew.bat :api:bootRun
```

API health check:

```powershell
curl.exe http://localhost:8080/actuator/health
```

## Demo Verification

SSE smoke script는 Kafka topic에 테스트 메시지를 넣고 API SSE로 수신되는지 확인합니다.

```powershell
.\scripts\e2e\stream-smoke.ps1 -Scenario all -TimeoutSeconds 5
```

개별 시나리오 목록:

```powershell
.\scripts\e2e\stream-smoke.ps1 -ListScenarios
```

스크립트 옵션 도움말:

```powershell
Get-Help .\scripts\e2e\stream-smoke.ps1 -Full
```

자세한 데모 절차와 장애 확인법은 [docs/DEMO.md](docs/DEMO.md)를 보세요.

## API

대표 API:

- `GET /api/v1/market/ticks/latest/{marketCodeId}`
- `GET /api/v1/market/premium/ranking?n=10`
- `GET /api/v1/market/premium-detail/raw`
- `GET /api/v1/analytics/candles`
- `GET /api/v1/analytics/indicators/latest`
- `GET /api/v1/stream/ticks?marketCodeId=9001`
- `GET /api/v1/stream/premium`
- `GET /api/v1/stream/candles/close?type=tick`

전체 엔드포인트는 [docs/API.md](docs/API.md)에 정리했습니다.

## Verified Scope

최근 로컬 Docker 환경에서 확인한 범위:

- `.\gradlew.bat compileJava`
- `.\gradlew.bat compileTestJava`
- PostgreSQL write: market-code, tick, premium, premium-detail, candle, indicator
- Redis latest/state key 생성
- Kafka topic publish/consume
- API health
- API SSE smoke: tick, premium, premium-detail, candle, indicator

전체 `test`는 Kafka/PostgreSQL/Redis/Testcontainers 상태에 영향을 받을 수 있으므로, 실패 시 모듈별로 나눠 확인합니다.

## MVP Scope

MVP에 포함:

- 거래소 raw market-code/tick 수집 경로
- FX 수집 및 premium 계산에 필요한 환율 cache
- tick, premium, premium-detail 저장 및 Kafka 발행
- tick/premium/premium-detail 기반 candle/indicator 저장 및 Kafka 발행
- REST 조회 API
- SSE 실시간 stream
- 로컬 Docker 기반 DB/Redis/Kafka/observability stack

MVP 제외:

- `trading` 모듈의 자동매매, 주문 실행, 포지션/리스크 관리
- proto 기반 직렬화, gRPC, schema registry 연동
- 경제지표의 실시간 downstream 분석/SSE 통합. 저장/조회 기반은 있으나 market data MVP의 핵심 E2E 범위는 아닙니다.
- 프론트엔드 대시보드. 화면은 별도 프로젝트에서 담당합니다.

이 항목들은 미완성으로 숨기는 대상이 아니라, 이번 MVP에서 의도적으로 제외한 확장 후보입니다.

## Documents

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/DEMO.md](docs/DEMO.md)
- [docs/API.md](docs/API.md)
- [docs/observability.md](docs/observability.md)
- [CLAUDE.md](CLAUDE.md)
