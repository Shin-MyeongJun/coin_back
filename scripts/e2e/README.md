# 스트림 스모크 테스트

이 폴더는 API SSE 스트림 경로를 빠르게 확인하는 작은 E2E 스모크 테스트 스크립트를 담고 있습니다.

이 스크립트는 실제 거래소 WebSocket에 연결하지 않습니다. 대신 Kafka 토픽에 테스트 JSON 메시지 1개를 직접 넣고, 실행 중인 API가 그 Kafka 레코드를 받아 SSE로 다시 내보내는지 확인합니다.

## 확인 시나리오

- `tick-raw`: `market-data.tick` -> `/api/v1/stream/ticks`
- `premium-raw`: `market-data.premium` -> `/api/v1/stream/premium`
- `premium-detail-raw`: `market-data.premium-detail` -> `/api/v1/stream/premium-detail/raw`
- `tick-candle`: `analytics.tick-candle` -> `/api/v1/stream/candles/close?type=tick`
- `premium-candle`: `analytics.premium-candle` -> `/api/v1/stream/candles/close?type=premium`
- `premium-detail-candle`: `analytics.premium-detail-candle` -> `/api/v1/stream/candles/close?type=premium-detail`
- `tick-indicator`: `analytics.tick-indicator` -> `/api/v1/stream/indicators/close?type=tick`
- `premium-indicator`: `analytics.premium-indicator` -> `/api/v1/stream/indicators/close?type=premium`

## 사전 준비

```powershell
docker compose -f .\docker\docker-compose.yml up -d zookeeper kafka postgres redis
.\gradlew.bat :api:bootRun
```

## 전체 시나리오 실행

```powershell
.\scripts\e2e\stream-smoke.ps1
```

PowerShell 실행 정책 때문에 스크립트가 막히면 다음처럼 실행합니다.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e\stream-smoke.ps1
```

## 단일 시나리오 실행

```powershell
.\scripts\e2e\stream-smoke.ps1 -Scenario premium-detail-candle
```

## API 주소 변경

```powershell
.\scripts\e2e\stream-smoke.ps1 -ApiBaseUrl http://localhost:8081
```

## 시나리오 목록 확인

Docker나 API를 건드리지 않고 사용 가능한 시나리오만 확인합니다.

```powershell
.\scripts\e2e\stream-smoke.ps1 -ListScenarios
```

## 성공 의미

```text
Kafka topic이 테스트 JSON을 받음
  -> API Kafka listener가 레코드를 역직렬화함
  -> API가 레코드를 인메모리 stream sink로 전달함
  -> SSE endpoint가 연결된 클라이언트로 레코드를 전송함
  -> 스크립트가 SSE 응답 라인을 캡처하고 기대 문자열을 찾음
```

## 검증하지 않는 범위

- 실제 거래소 WebSocket ingestion이 동작하는지는 검증하지 않습니다.
- analytics가 실시간 tick으로 candle을 계산하는지는 검증하지 않습니다.
- PostgreSQL이나 Redis 영속화가 정확한지는 검증하지 않습니다.

이 스크립트는 의도적으로 Kafka -> API -> SSE 경로만 좁게 확인하는 스모크 테스트입니다.
