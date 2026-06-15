# CoinData Demo Guide

이 문서는 로컬 Docker 환경에서 CoinData 백엔드 MVP가 실제로 흐르는지 확인하는 절차입니다.

## Demo Goal

데모에서 보여줄 핵심은 다음 한 줄입니다.

```text
Kafka에 market data event를 넣으면 DB/Redis/Kafka/API SSE까지 이어진다.
```

확인 범위:

- PostgreSQL write
- Redis latest/state key
- Kafka topic publish/consume
- API health
- API SSE

## Prerequisites

- Windows PowerShell
- Docker Desktop
- Java 21
- Gradle wrapper 사용 가능
- `curl.exe`
- 로컬 포트
  - PostgreSQL: `5432`
  - Redis: `6379`
  - Kafka: `9092`
  - API: `8080`
  - Prometheus: `9090`
  - Grafana: `3000`

## Start Infrastructure

저장소 루트에서 실행합니다.

```powershell
docker compose --env-file .env -f docker/docker-compose.yml -f docker/docker-compose.observability.yml up -d
```

상태 확인:

```powershell
docker ps
docker exec redis redis-cli ping
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
docker exec -e PGPASSWORD=$env:APP_DB_PASSWORD postgres_db psql -U $env:APP_DB_USERNAME -d $env:APP_DB_NAME -c "\dt"
```

`redis-cli ping` 결과가 `PONG`이면 Redis는 정상입니다.

## Environment Variables

핵심 모듈 실행 전에 같은 터미널 또는 각 터미널에 아래 값을 넣습니다.

```powershell
Get-Content .env |
  Where-Object { $_ -and -not $_.TrimStart().StartsWith("#") -and $_.Contains("=") } |
  ForEach-Object {
    $name, $value = $_ -split "=", 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
  }
```

주의: API의 datasource URL은 `.env`의 `APP_DB_URL`에서만 주입됩니다.

## Start Core Modules

아래 네 모듈을 별도 PowerShell 창에서 실행합니다.

```powershell
.\gradlew.bat :meta_data:bootRun
```

```powershell
.\gradlew.bat :market_data:bootRun
```

```powershell
.\gradlew.bat :analytics:bootRun
```

```powershell
.\gradlew.bat :api:bootRun
```

API health:

```powershell
curl.exe http://localhost:8080/actuator/health
```

기대 결과:

```json
{"status":"UP"}
```

## SSE Smoke Script

가장 빠른 데모 검증은 `scripts/e2e/stream-smoke.ps1`입니다.

```powershell
.\scripts\e2e\stream-smoke.ps1 -Scenario all -TimeoutSeconds 5
```

이 스크립트는 다음 일을 합니다.

1. API health를 확인합니다.
2. Kafka container가 있는지 확인합니다.
3. 필요한 Kafka topic을 생성하거나 확인합니다.
4. SSE endpoint를 `curl.exe`로 열어 둡니다.
5. Kafka topic에 테스트 메시지를 1개 넣습니다.
6. SSE output에 기대 문자열이 들어왔는지 확인합니다.

전체 시나리오:

| Scenario | Kafka topic | SSE endpoint |
| --- | --- | --- |
| `tick-raw` | `market-data.tick` | `/api/v1/stream/ticks?marketCodeId=9001` |
| `premium-raw` | `market-data.premium` | `/api/v1/stream/premium` |
| `premium-detail-raw` | `market-data.premium-detail` | `/api/v1/stream/premium-detail/raw` |
| `tick-candle` | `analytics.tick-candle` | `/api/v1/stream/candles/close?type=tick` |
| `premium-candle` | `analytics.premium-candle` | `/api/v1/stream/candles/close?type=premium` |
| `premium-detail-candle` | `analytics.premium-detail-candle` | `/api/v1/stream/candles/close?type=premium-detail` |
| `tick-indicator` | `analytics.tick-indicator` | `/api/v1/stream/indicators/close?type=tick` |
| `premium-indicator` | `analytics.premium-indicator` | `/api/v1/stream/indicators/close?type=premium` |

개별 시나리오 실행:

```powershell
.\scripts\e2e\stream-smoke.ps1 -Scenario tick-raw -TimeoutSeconds 5
```

시나리오 목록 확인:

```powershell
.\scripts\e2e\stream-smoke.ps1 -ListScenarios
```

API health check를 건너뛰고 싶은 경우:

```powershell
.\scripts\e2e\stream-smoke.ps1 -Scenario all -SkipHealthCheck
```

Kafka container 이름이나 bootstrap server가 다르면:

```powershell
.\scripts\e2e\stream-smoke.ps1 `
  -KafkaContainer kafka `
  -KafkaBootstrap localhost:9092 `
  -ApiBaseUrl http://localhost:8080
```

## Expected SSE Output

SSE는 연결 직후 `connected` event를 먼저 보냅니다.

```text
event:connected
data:{}
```

그 뒤 실제 data event가 옵니다.

```text
event:tick
data:{"marketCodeId":9001,"bid":"100.00","ask":"101.00","timestamp":1710000000000}
```

`connected` event는 연결 생존 확인용입니다. 실제 데이터 검증은 각 scenario의 expected JSON fragment로 확인합니다.

## Full Pipeline Demo

`stream-smoke.ps1`는 API SSE smoke에 집중합니다. ingestion -> market_data -> analytics까지 전체 파이프라인을 직접 보여주려면 다음 흐름으로 테스트 데이터를 넣습니다.

1. `ingestion-exchange.market-code-raw`에 Upbit/Binance synthetic market-code 입력
2. `ingestion-fx.fx`에 `KRW/USD` FX 입력
3. `ingestion-exchange.tick-raw`에 양쪽 거래소 tick 입력
4. 한쪽 tick을 한 번 더 입력해서 premium 계산 트리거
5. DB `tick`, `premium`, `premium_detail` 확인
6. 다음 1분 bucket close 이후 `analytics.*` topic과 candle/indicator table 확인

테스트 symbol은 실제 거래소 symbol과 충돌하지 않게 `E2EBTC` 같은 synthetic 값을 쓰는 편이 좋습니다.

## Useful Checks

Kafka topics:

```powershell
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Kafka consumer group:

```powershell
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

Kafka message sample:

```powershell
docker exec kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic market-data.premium `
  --from-beginning `
  --timeout-ms 5000
```

Redis keys:

```powershell
docker exec redis redis-cli --scan --pattern "ys:local:v1:*"
```

PostgreSQL counts:

```powershell
docker exec -e PGPASSWORD=$env:APP_DB_PASSWORD postgres_db psql -U $env:APP_DB_USERNAME -d $env:APP_DB_NAME -c "select count(*) from tick;"
docker exec -e PGPASSWORD=$env:APP_DB_PASSWORD postgres_db psql -U $env:APP_DB_USERNAME -d $env:APP_DB_NAME -c "select count(*) from premium;"
docker exec -e PGPASSWORD=$env:APP_DB_PASSWORD postgres_db psql -U $env:APP_DB_USERNAME -d $env:APP_DB_NAME -c "select count(*) from tick_candle;"
docker exec -e PGPASSWORD=$env:APP_DB_PASSWORD postgres_db psql -U $env:APP_DB_USERNAME -d $env:APP_DB_NAME -c "select count(*) from premium_indicator;"
```

## Troubleshooting

### API health fails

- API가 실행 중인지 확인합니다.
- `APP_DB_URL`, `APP_DB_USERNAME`, `APP_DB_PASSWORD`가 `coin_data` DB를 가리키는지 확인합니다.
- `docker ps`에서 `postgres_db`, `redis`, `kafka`가 모두 Up인지 확인합니다.

### SSE connects but no event arrives

- Kafka topic 이름이 맞는지 확인합니다.
- API stream consumer가 같은 Kafka bootstrap server를 보고 있는지 확인합니다.
- `stream-smoke.ps1`의 scenario가 기대하는 endpoint와 topic을 확인합니다.

### Premium이 생성되지 않음

- 양쪽 거래소의 market-code가 먼저 저장되어야 합니다.
- FX cache가 필요한 방향으로 들어가 있어야 합니다.
- tick 입력 순서에 따라 계산 방향이 달라질 수 있으므로, 양쪽 tick 입력 후 기준 거래소 tick을 한 번 더 넣어 트리거합니다.

### Analytics candle/indicator가 생성되지 않음

- analytics consumer group이 partition을 할당받았는지 확인합니다.
- `analytics` 모듈 로그에서 partition store 준비 로그와 skip 로그를 확인합니다.
- 1분 candle은 bucket close timing을 기다려야 합니다.

## Verified Commands

문서 작성 시점에 사용한 기본 검증 명령:

```powershell
.\gradlew.bat compileJava --console=plain
.\gradlew.bat compileTestJava --console=plain
.\scripts\e2e\stream-smoke.ps1 -Scenario all -TimeoutSeconds 5
```
