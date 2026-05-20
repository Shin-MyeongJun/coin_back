# CoinData API

Base URL:

```text
http://localhost:8080
```

API는 크게 REST 조회와 SSE stream으로 나뉩니다.

## Health

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/actuator/health` | API health |
| `GET` | `/actuator/prometheus` | Prometheus metrics |

## Metadata REST

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/meta/exchanges` | - | exchange list |
| `GET` | `/api/v1/meta/exchanges/{exchangeId}/markets` | path `exchangeId` | exchange별 market-code list |
| `GET` | `/api/v1/meta/markets/search` | `q`, optional `exchange` | market-code search |
| `GET` | `/api/v1/meta/integrity` | - | metadata mapping integrity check |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/meta/markets/search?q=BTC"
```

## Market Data REST

### Tick

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/market/ticks/latest/{marketCodeId}` | path `marketCodeId` | latest tick 단건 |
| `GET` | `/api/v1/market/ticks/latest` | `marketCodeIds=1,2,3` | latest tick bulk |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/market/ticks/latest/9001"
curl.exe "http://localhost:8080/api/v1/market/ticks/latest?marketCodeIds=9001,9002"
```

### Premium

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/market/premium/snapshot/{base}` | path `base` | base symbol 기준 premium snapshot |
| `GET` | `/api/v1/market/premium/series` | `baseExchangeId`, `compareExchangeId`, `symbol`, `bucketSeconds`, `fromTs`, `toTs` | premium time series |
| `GET` | `/api/v1/market/premium/ranking` | optional `n`, default `10` | premium ranking |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/market/premium/ranking?n=10"
curl.exe "http://localhost:8080/api/v1/market/premium/series?baseExchangeId=2&compareExchangeId=1602&symbol=E2EBTC&bucketSeconds=60&fromTs=1710000000000&toTs=1710003600000"
```

### Premium Detail

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/market/premium-detail/raw` | `baseExchangeId`, `compareExchangeId`, `symbol`, `fromTs`, `toTs` | premium-detail raw rows |
| `GET` | `/api/v1/market/premium-detail/agg` | `baseExchangeId`, `compareExchangeId`, `symbol`, `bucketSeconds`, `fromTs`, `toTs` | premium-detail aggregated series |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/market/premium-detail/raw?baseExchangeId=2&compareExchangeId=1602&symbol=E2EBTC&fromTs=1710000000000&toTs=1710003600000"
```

### FX

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/market/fx/latest` | `baseCurrency`, `quoteCurrency` | latest FX |
| `GET` | `/api/v1/market/fx/raw` | `baseCurrency`, `quoteCurrency`, `fromTs`, `toTs` | raw FX rows |
| `GET` | `/api/v1/market/fx/downsampled` | `baseCurrency`, `quoteCurrency`, `bucketSeconds`, `fromTs`, `toTs` | downsampled FX |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/market/fx/latest?baseCurrency=KRW&quoteCurrency=USD"
```

## Analytics REST

### Candles

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/analytics/candles` | `type`, `interval`, `fromTs`, `toTs`, plus target id fields | candle series |
| `GET` | `/api/v1/analytics/candles/mini` | `type`, `interval`, optional `limit`, plus target id fields | mini chart candles |
| `GET` | `/api/v1/analytics/candles/downsampled` | `type`, `sourceInterval`, `targetBucketSeconds`, `fromTs`, `toTs`, plus target id fields | downsampled candles |
| `GET` | `/api/v1/analytics/candles/last-closed` | `type`, `interval`, plus target id fields | last closed bucket |

`type=tick` target:

- `marketCodeId`

`type=premium` target:

- `symbol`
- `baseExchangeId`
- `compareExchangeId`

`type=premium-detail` is supported for `/last-closed`.

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/analytics/candles?type=tick&marketCodeId=9001&interval=1m&fromTs=1710000000000&toTs=1710003600000"
curl.exe "http://localhost:8080/api/v1/analytics/candles/last-closed?type=premium-detail&symbol=E2EBTC&baseExchangeId=2&compareExchangeId=1602&interval=1m"
```

### Indicators

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/analytics/indicators` | `type`, `interval`, `indicatorType`, `fromTs`, `toTs`, plus target id fields | indicator series |
| `GET` | `/api/v1/analytics/indicators/latest` | `type`, `interval`, `indicatorType`, plus target id fields | latest indicator |
| `GET` | `/api/v1/analytics/indicators/latest/multi` | `marketCodeIds`, `interval`, `indicatorType` | latest tick indicator for multiple markets |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/analytics/indicators/latest?type=tick&marketCodeId=9001&interval=1m&indicatorType=EMA"
curl.exe "http://localhost:8080/api/v1/analytics/indicators/latest/multi?marketCodeIds=9001,9002&interval=1m&indicatorType=RSI"
```

### Screener

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/analytics/screener` | `type`, `interval`, `indicatorType`, optional `period`, `minValue`, `maxValue` | tick/premium indicator screener |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/analytics/screener?type=tick&interval=1m&indicatorType=RSI&minValue=30&maxValue=70"
```

## Economic REST

경제지표 조회 API는 존재하지만, market data MVP의 핵심 E2E demo 범위는 아닙니다.

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/economic/indicators` | optional `category` | indicator metadata list |
| `GET` | `/api/v1/economic/indicators/{codeId}` | path `codeId` | indicator metadata |
| `GET` | `/api/v1/economic/indicators/{codeId}/series` | `fromTs`, `toTs` | indicator series |
| `GET` | `/api/v1/economic/indicators/{codeId}/change-rate` | path `codeId` | change-rate list |
| `GET` | `/api/v1/economic/calendar` | `fromTs`, `toTs` | economic calendar |
| `GET` | `/api/v1/economic/correlation` | `asset` | economic correlation |

## Composition REST

프론트엔드나 dashboard 프로젝트에서 한 번에 필요한 데이터를 가져가기 위한 composition API입니다.

| Method | Path | Parameters | Description |
| --- | --- | --- | --- |
| `GET` | `/api/v1/compose/market-overview/{marketCodeId}` | `base`, optional `indicatorMarketCodeIds`, optional `interval`, optional `indicatorType` | market overview |
| `GET` | `/api/v1/compose/chart/{marketCodeId}` | `interval`, optional `indicatorType`, `fromTs`, `toTs` | chart bundle |
| `GET` | `/api/v1/compose/dashboard` | `marketCodeIds`, optional `rankingLimit`, `calendarFromTs`, `calendarToTs` | dashboard bundle |

Example:

```powershell
curl.exe "http://localhost:8080/api/v1/compose/market-overview/9001?base=E2EBTC&interval=1m&indicatorType=RSI"
```

## SSE Stream API

All SSE endpoints return `text/event-stream`. Market/analytics streams below are public read streams. The alert stream is a private JWT account stream and is not part of the public `/api/v1/stream/*` permit-list.

| Method | Path | Parameters | Kafka source | Event names |
| --- | --- | --- | --- | --- |
| `GET` | `/api/v1/stream/ticks` | optional `marketCodeId` | `market-data.tick` | `connected`, `tick` |
| `GET` | `/api/v1/stream/premium` | - | `market-data.premium` | `connected`, `premium` |
| `GET` | `/api/v1/stream/premium-detail/raw` | - | `market-data.premium-detail` | `connected`, `premium-detail` |
| `GET` | `/api/v1/stream/candles/close` | optional `type`, default `tick` | `analytics.*-candle` | `connected`, `tick-candle`, `premium-candle`, `premium-detail-candle` |
| `GET` | `/api/v1/stream/indicators/close` | optional `type`, default `tick` | `analytics.*-indicator` | `connected`, `tick-indicator`, `premium-indicator` |
| `GET` | `/api/v1/stream/alerts` | JWT `Authorization: Bearer ...` or `?access_token=` | `alert.firing` / in-process SSE registry | `alert-firing` |

Supported candle `type`:

- `tick`
- `premium`
- `premium-detail`

Supported indicator `type`:

- `tick`
- `premium`

SSE example:

```powershell
curl.exe --no-buffer -H "Accept: text/event-stream" "http://localhost:8080/api/v1/stream/ticks?marketCodeId=9001"
```

Expected shape:

```text
event:connected
data:{}

event:tick
data:{"marketCodeId":9001,"bid":"100.00","ask":"101.00","timestamp":1710000000000}
```

## SSE Smoke Test

The smoke script opens an SSE stream, produces one Kafka message, and checks that the expected SSE data arrives.

```powershell
.\scripts\e2e\stream-smoke.ps1 -Scenario all -TimeoutSeconds 5
```

Available scenarios:

```powershell
.\scripts\e2e\stream-smoke.ps1 -ListScenarios
```

자세한 사용법은 [DEMO.md](DEMO.md)를 보세요.

## Error Behavior

- 단건 조회 결과가 없으면 controller에서 `NoSuchElementException`을 던지고 global exception handler가 응답을 만듭니다.
- 숫자 query parameter parsing 실패는 Spring MVC validation/conversion error로 처리됩니다.
- SSE stream은 연결 직후 `connected` event를 전송합니다. 클라이언트는 이 event를 데이터로 집계하지 않는 편이 좋습니다.
