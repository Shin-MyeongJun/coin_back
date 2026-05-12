# 런타임 실행 스크립트

이 스크립트들은 로컬 Spring Boot 실행 모듈을 Gradle `bootRun`으로 시작하고 중지합니다.

## 다중 실행 정책

여러 인스턴스 실행을 허용하는 모듈은 아래 3개입니다.

- `market_data`
- `analytics`
- `api`

나머지 실행 모듈은 기본 1개만 실행합니다. 수집, 메타데이터 저장, 예약 수집 작업은 프로세스를 여러 개 띄우면 중복 수집이나 중복 저장이 생길 수 있기 때문입니다.

## 핵심 런타임 시작

```powershell
.\scripts\run\start-runtime.ps1
```

기본 실행 모듈은 아래와 같습니다.

- `meta_data`
- `market_data`
- `analytics`
- `api`

## 다중 인스턴스 시작

```powershell
.\scripts\run\start-runtime.ps1 `
  -MarketDataInstances 2 `
  -AnalyticsInstances 2 `
  -ApiInstances 2 `
  -ApiBasePort 8080
```

API 인스턴스는 `ApiBasePort`부터 `API_SERVER_PORT` 값을 순서대로 사용합니다. 위 예시는 API를 `8080`, `8081`에 띄웁니다.

## 수집 모듈 추가 실행

```powershell
.\scripts\run\start-runtime.ps1 -IncludeIngestion
.\scripts\run\start-runtime.ps1 -IncludeEconomic
```

실행 가능한 모든 모듈을 시작하려면 다음 옵션을 사용합니다.

```powershell
.\scripts\run\start-runtime.ps1 -All
```

## 모듈 별칭

시작/중지 스크립트는 짧은 별칭도 받습니다.

- `market` -> `market_data`
- `meta` -> `meta_data`
- `upbit` -> `upbit_ingestion`
- `binance` -> `binance_ingestion`
- `fx` -> `fx_ingestion`

예시:

```powershell
.\scripts\run\start-runtime.ps1 -Modules market,analytics,api -MarketDataInstances 2 -AnalyticsInstances 2 -ApiInstances 3
```

## 로그와 PID 파일

런타임 로그는 아래 경로에 남습니다.

```text
build\run-logs
```

PID 파일은 아래 경로에 남습니다.

```text
build\run-pids
```

프로세스별 실행 명령 파일은 아래 경로에 생성됩니다.

```text
build\run-commands
```

## 런타임 중지

런타임 스크립트로 시작한 프로세스를 모두 중지합니다.

```powershell
.\scripts\run\stop-runtime.ps1
```

특정 모듈만 중지할 수도 있습니다.

```powershell
.\scripts\run\stop-runtime.ps1 -Modules api,analytics
```
