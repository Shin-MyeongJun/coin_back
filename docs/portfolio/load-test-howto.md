# Tick Raw Load Test How-To

이 문서는 포트폴리오의 "초당 15k~30k 메시지" 주장을 재현 가능한 방식으로 남기기 위한 실행 절차와 해석 기준이다. 부하 대상 Kafka 토픽은 운영 파이프라인과 같은 `ingestion-exchange.tick-raw`를 사용한다.

## 사전 조건

- PowerShell 7.x
- Java 21과 `javac`가 PATH에 있어야 한다.
- Docker Desktop 메모리 8GB 이상 권장, 12GB 이상이면 더 안정적이다.
- Docker 디스크 여유 공간 20GB 이상 권장. `ingestion-exchange.tick-raw`는 retention bytes가 20GB로 잡혀 있다.
- `docker compose`가 사용 가능해야 한다.
- Grafana Render API용 `grafana-image-renderer` 컨테이너가 observability compose에 포함되어 있어야 한다.
- Grafana 기본 계정은 로컬 compose 기준 `admin/admin`이다. 다른 값을 쓰는 경우 `GRAFANA_USER`, `GRAFANA_PASS` 환경 변수로 override한다.

## 단독 부하 발행

```powershell
.\scripts\load\produce-tick-raw.ps1 `
  -TargetTps 15000 `
  -DurationSec 60 `
  -BootstrapServers localhost:9092 `
  -Topic ingestion-exchange.tick-raw
```

`scripts/load/produce-tick-raw.ps1`는 `scripts/load/LoadProducer.java`를 컴파일한 뒤 실행한다. `LoadProducer`는 Spring 없이 Apache Kafka `KafkaProducer`를 직접 사용하고, `com.example.demo.contracts.message.raw.TickRawMessage` record를 만들어 JSON으로 직렬화한다.

진행 중에는 stderr에 1초 단위 `sent`, `acked`, `sendTps`, `ackTps`, `errors`가 출력된다. 종료 시 stdout에는 `averageSendTps`, `averageAckTps`, `p95ProduceLatencyMs`를 포함한 JSON summary 한 줄이 출력된다.

## Grafana 패널 캡처

```powershell
.\scripts\load\capture-grafana.ps1
```

기본 저장 경로는 `docs/portfolio/screenshots/grafana`이다. 파일명은 `<panel-name>-<yyyyMMdd-HHmm>.png` 형식이며, Render API 시간 범위는 `from=now-10m&to=now`로 고정된다.

단일 패널만 확인하려면 다음처럼 실행한다.

```powershell
.\scripts\load\capture-grafana.ps1 -PanelName "Kafka Consumer Lag by Topic"
```

기본 캡처 대상은 다음이다.

| 패널 | 근거 |
| --- | --- |
| Kafka Consumer Lag by Topic | `ingestion-exchange.tick-raw` 입력 부하가 downstream 처리량을 초과할 때 lag가 증가하는지 확인한다. |
| Kafka Consumer Lag by Group | `market-data.tick-raw.handle` 등 consumer group별 병목 위치를 확인한다. |
| Redis Memory Usage | downstream에서 Redis latest cache를 갱신하는 경우 메모리 사용량 변화를 확인한다. |
| Redis Command Throughput | Redis write/read 처리량이 부하 시간대와 함께 움직이는지 확인한다. |
| API JVM panels | `coindata-api-overview.json`가 provision된 경우 JVM memory, GC, CPU, HTTP p95, Kafka consumer throughput을 함께 캡처한다. |

## 전체 벤치마크 1회 실행

```powershell
.\scripts\load\run-full-benchmark.ps1
```

기본 시나리오는 다음 순서로 실행된다.

1. `docker compose -f docker/docker-compose.yml -f docker/docker-compose.observability.yml up -d`
2. `scripts/run/start-runtime.ps1 -All`
3. Prometheus `http://localhost:9090/-/healthy`와 Grafana health 확인
4. warmup: 5k TPS, 30초
5. main load: 30k TPS, 120초
6. main load 90초 시점 Grafana 캡처
7. cool-down 30초 후 Grafana 재캡처
8. 결과 폴더에 `run-metadata.json` 저장

결과 폴더 기본값은 `docs/portfolio/screenshots/grafana/run-<yyyyMMdd-HHmmss>`이다. `run-metadata.json`에는 시작/종료 시각, 요청 TPS, 실제 발행/ack TPS, p95 produce latency, hostname, OS, CPU, RAM, Java version, 로그 파일 경로, 캡처 파일 경로가 들어간다.

## 결과 해석

- 1분 단독 실행에서 `averageSendTps`가 목표 TPS의 ±5% 범위면 producer rate control은 통과로 본다.
- main load 중 `Kafka Consumer Lag by Topic`에서 `ingestion-exchange.tick-raw` lag가 증가하고, cool-down 이후 같은 패널에서 감소하면 "30k 입력을 받아 downstream이 backlog를 회복한다"는 근거로 쓸 수 있다.
- `Kafka Consumer Lag by Group`은 병목 consumer group을 특정하는 자료다. `market-data.tick-raw.handle` lag가 높다면 raw tick consumer 처리량이 핵심 병목이다.
- `Redis Command Throughput`과 `Redis Memory Usage`는 downstream cache write가 실제로 발생했는지 보조 근거로 쓴다. 가상 marketCode가 metadata cache에 없으면 market_data 단계에서 drop될 수 있어 Redis 변화가 작을 수 있다.
- API JVM 패널은 API가 켜져 있고 Prometheus `coindata-api` target이 UP일 때만 의미가 있다. HTTP p95 패널은 Micrometer histogram bucket이 노출되는 환경에서 p95가 채워진다.

## 검증 포인트

- [ ] `scripts/load/produce-tick-raw.ps1 -TargetTps 15000 -DurationSec 60` 실행 시 목표 TPS의 ±5% 범위 유지
- [ ] main load 중 Kafka consumer lag 증가, cool-down 후 감소
- [ ] 캡처 PNG 해상도 1920x1080에서 시간 축 라벨 확인 가능
- [ ] `run-metadata.json`에 hostname, OS, CPU, RAM, Java version 기록
- [ ] `scripts/run/start-runtime.ps1 -All`이 이미 실행 중인 프로세스를 skip하는지 확인
- [ ] 모든 스크립트 PowerShell 7.x에서 실행
