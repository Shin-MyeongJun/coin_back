# Stream smoke test

This folder contains a small end-to-end smoke script for the API SSE stream.

The script does not connect to real exchange WebSockets. Instead, it injects one JSON message directly into Kafka and checks whether the running API receives that Kafka record and emits it through SSE.

Covered scenarios:

- `tick-raw`: `market-data.tick` -> `/api/v1/stream/ticks`
- `premium-raw`: `market-data.premium` -> `/api/v1/stream/premium`
- `premium-detail-raw`: `market-data.premium-detail` -> `/api/v1/stream/premium-detail/raw`
- `tick-candle`: `analytics.tick-candle` -> `/api/v1/stream/candles/close?type=tick`
- `premium-candle`: `analytics.premium-candle` -> `/api/v1/stream/candles/close?type=premium`
- `premium-detail-candle`: `analytics.premium-detail-candle` -> `/api/v1/stream/candles/close?type=premium-detail`
- `tick-indicator`: `analytics.tick-indicator` -> `/api/v1/stream/indicators/close?type=tick`
- `premium-indicator`: `analytics.premium-indicator` -> `/api/v1/stream/indicators/close?type=premium`

Prerequisites:

```powershell
docker compose -f .\docker\docker-compose.yml up -d zookeeper kafka postgres redis
.\gradlew.bat :api:bootRun
```

Run every scenario:

```powershell
.\scripts\e2e\stream-smoke.ps1
```

If PowerShell blocks script execution:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e\stream-smoke.ps1
```

Run one scenario:

```powershell
.\scripts\e2e\stream-smoke.ps1 -Scenario premium-detail-candle
```

Use a custom API URL:

```powershell
.\scripts\e2e\stream-smoke.ps1 -ApiBaseUrl http://localhost:8081
```

List available scenarios without touching Docker or the API:

```powershell
.\scripts\e2e\stream-smoke.ps1 -ListScenarios
```

What a pass means:

```text
Kafka topic receives test JSON
  -> API Kafka listener deserializes the record
  -> API emits the record into the in-memory stream sink
  -> SSE endpoint sends the record to a connected client
  -> script captures the SSE line and finds expected text
```

What this does not prove:

- It does not prove that real exchange WebSocket ingestion is working.
- It does not prove that analytics can compute candles from live ticks.
- It does not prove that PostgreSQL or Redis persistence is correct.

It is intentionally a narrow smoke test for the Kafka-to-API-SSE path.
