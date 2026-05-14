# Kafka Retention Policy

> 2026-05-14 기준. 토픽 설정 변경 시 이 문서와 각 모듈의 `*KafkaAdminConfig.java`를 함께 갱신한다.

---

## 1. 결정 원칙

**retention = 다운스트림이 장애로부터 복구를 시도하는 데 허용할 수 있는 최대 시간.**

복구 윈도우보다 짧으면 메시지가 사라져 재처리 기회를 잃는다. 복구 윈도우보다 길면 디스크와 브로커 메모리가 낭비된다. retention.ms는 "얼마나 오래 실패해도 되는가"에 대한 명시적 SLA다.

---

## 2. 도메인별 replay 윈도우 설계

### raw (ingestion 계열)

`ingestion-exchange.tick-raw`는 1시간 retention이다. 수집기가 1시간 이내 복구되면 market_data가 재처리할 수 있다. 1시간을 넘기면 손실을 수용한다. 실시간성이 핵심이라 오래된 raw tick은 재처리 가치가 낮기 때문이다.

`ingestion-exchange.market-code-raw`는 compact 정책이다. 거래소 마켓 코드는 자주 바뀌지 않는 마스터 데이터이므로 키별 최신 레코드만 보존하면 충분하다.

`ingestion-fx.fx`는 2시간이다. FX 데이터는 tick보다 갱신 주기가 길어 2시간 내 복구 시 계산 공백을 메울 수 있다.

### market-data 계열

`market-data.tick`은 6시간이다. analytics 인스턴스가 6시간 이내 재기동되면 Kafka에서 직접 replay할 수 있다. 6시간은 심야 유지보수나 단일 노드 장애를 커버하는 실용적 기준이다.

`market-data.premium` / `market-data.premium-detail`은 각각 6시간 / 3시간이다. premium-detail은 tick 대비 배수 파티션을 사용해 볼륨이 크므로 retention을 3시간으로 줄였다.

### analytics 계열

`analytics.*-candle` / `analytics.*-indicator` 5종은 모두 24시간이다. candle/indicator는 analytics store가 손상되거나 초기화됐을 때 Kafka replay로 상태를 복원한다. 24시간은 DB flush 이후 상태 복원에 충분한 윈도우이며, API SSE fan-out 클라이언트가 연결을 재수립하는 시간도 커버한다.

### economic 계열

`economic-ind.indicator`는 7일이다. 경제지표는 발표 주기가 주 단위이고 레코드 수가 적다. 다운스트림 consumer(미래 확장)가 오랫동안 오프라인이어도 재처리 가능하도록 여유를 뒀다.

### 운영 상태 (heartbeat) 계열

`infra.heartbeat` / `infra.health-change`는 30분이다. 모듈 상태는 실시간 공유가 목적이므로 오래된 데이터는 의미가 없다. 30분을 넘긴 heartbeat는 이미 장애 감지가 끝난 후다.

### user 계열 (미래 확장, §6 참조)

user 관련 토픽은 현재 MVP에 없다. 추가 시 §6의 가이드를 따른다.

---

## 3. 적용 정책 표

| 토픽 | retention.ms | cleanup.policy | retention.bytes | segment.ms | 비고 |
|---|---:|---|---:|---:|---|
| `ingestion-exchange.tick-raw` | 3,600,000 (1h) | delete | 21,474,836,480 (20GB) | 1,800,000 (30m) | 일회용 raw |
| `ingestion-exchange.market-code-raw` | — | compact | — | — | 키별 최신 |
| `ingestion-fx.fx` | 7,200,000 (2h) | delete | 2,147,483,648 (2GB) | 1,800,000 (30m) | raw성 |
| `market-data.tick` | 21,600,000 (6h) | delete | 32,212,254,720 (30GB) | 3,600,000 (1h) | fan-out 다수 |
| `market-data.premium` | 21,600,000 (6h) | delete | 10,737,418,240 (10GB) | 3,600,000 (1h) | |
| `market-data.premium-detail` | 10,800,000 (3h) | delete | 16,106,127,360 (15GB) | 3,600,000 (1h) | 볼륨 큼 |
| `meta-data.exchange` | — | compact | — | — | 마스터 |
| `meta-data.market-code` | — | compact | — | — | 마스터 |
| `analytics.tick-candle` | 86,400,000 (24h) | delete | 5,368,709,120 (5GB) | 3,600,000 (1h) | Kafka replay 윈도우 |
| `analytics.premium-candle` | 86,400,000 (24h) | delete | 5,368,709,120 (5GB) | 3,600,000 (1h) | |
| `analytics.premium-detail-candle` | 86,400,000 (24h) | delete | 5,368,709,120 (5GB) | 3,600,000 (1h) | |
| `analytics.tick-indicator` | 86,400,000 (24h) | delete | 5,368,709,120 (5GB) | 3,600,000 (1h) | |
| `analytics.premium-indicator` | 86,400,000 (24h) | delete | 5,368,709,120 (5GB) | 3,600,000 (1h) | |
| `economic-ind.indicator` | 604,800,000 (7d) | delete | 1,073,741,824 (1GB) | 86,400,000 (1d) | 양 작음 |
| `infra.heartbeat` | 1,800,000 (30m) | delete | — | 600,000 (10m) | 실시간 상태 공유 |
| `infra.health-change` | 1,800,000 (30m) | delete | — | 600,000 (10m) | 실시간 상태 공유 |
| `*.DLT` (있으면) | 1,209,600,000 (14d) | delete | 1,073,741,824 (1GB) | — | 장애 분석 |

**Producer 공통:** `compression.type=lz4`. `linger.ms` / `batch.size`는 기존 설정 유지.

---

## 4. compression / retention.bytes / segment.ms 3종 안전장치

### retention.ms만으로는 부족한 이유

Kafka는 retention.ms 기준으로 세그먼트 단위 삭제를 수행한다. 세그먼트가 닫히지 않으면 삭제가 지연된다. 트래픽이 많은 토픽에서 세그먼트가 매우 커지면 retention.ms가 지나도 실제 디스크 회수가 늦어지거나, 반대로 트래픽이 적은 토픽은 세그먼트가 닫히지 않아 오래된 데이터가 영구 잔존할 수 있다.

### segment.ms

세그먼트의 최대 수명을 강제한다. `ingestion-exchange.tick-raw`의 segment.ms=30m은 트래픽이 없어도 30분마다 세그먼트를 롤링해 retention.ms=1h와 조합 시 최대 2h 내에 정리된다. market-data 계열 segment.ms=1h는 1h retention 토픽에서 최악 2h 지연을 허용하는 수준이다.

### retention.bytes

파티션당 최대 보관 크기다. 트래픽 급증(거래소 변동성 폭발 등) 시 retention.ms에 도달하기 전에 디스크 임계에 도달할 수 있다. retention.bytes는 시간 기반 삭제보다 먼저 트리거돼 브로커 디스크를 보호한다. compact 토픽(`meta-data.*`, `ingestion-exchange.market-code-raw`)은 크기가 키 수에 비례하므로 retention.bytes를 별도 지정하지 않는다.

### compression.type=lz4

JSON 직렬화 페이로드는 텍스트이므로 lz4 압축 효율이 높다. lz4는 snappy 대비 압축률이 유사하거나 높고 CPU 부하가 낮다. producer 측에서 압축하면 브로커 수신 데이터 크기가 줄어 retention.bytes 소진 속도도 낮아진다.

---

## 5. 장애 시나리오별 영향

### analytics 인스턴스 6시간 다운

`market-data.tick` retention이 6h이므로, analytics가 6시간 내에 재기동되면 consumer group offset부터 replay가 가능하다. 6시간을 초과하면 tick 손실이 발생하고 해당 구간 candle/indicator는 공백이 생긴다. 이 경우 DB에 flush된 이전 candle 데이터는 유지되지만 공백 구간은 재계산 불가다.

### analytics store 손상 (Redis state 소실)

`analytics.*-candle` / `analytics.*-indicator` 5종은 24h retention이다. analytics가 재기동 시 Redis state를 복원하지 못하더라도, Kafka earliest offset부터 replay해 최대 24h치 candle/indicator 상태를 재구성할 수 있다. Redis가 동시에 정상이면 state cache hit으로 재구성 비용을 줄인다.

### tick-raw consumer 1시간 다운

`ingestion-exchange.tick-raw` retention이 1h이므로, market_data consumer가 1시간 내에 복구되면 밀린 tick을 전량 재처리할 수 있다. 1시간을 초과하면 그 이후 도착 분부터 처리를 재개하고, 손실 구간의 tick은 수용한다. 이 설계는 "raw tick은 실시간 가치가 핵심이며 오래된 raw를 재처리하는 비용이 이득보다 크다"는 판단에 기반한다.

---

## 6. 향후 user 계열 토픽 정책 가이드

**원칙: user의 source of truth는 RDB(PostgreSQL)다. Kafka user 토픽은 서비스 간 fan-out 목적이며 영구 저장소가 아니다.**

| 카테고리 | 권장 retention | cleanup.policy | 비고 |
|---|---|---|---|
| 감사 (audit log) | 30d | delete | 법적 요건 시 별도 cold storage로 이관 |
| 사용량 집계 (API usage) | 7d | delete | 집계 완료 후 RDB에 저장, Kafka는 버퍼 |
| 알림 이벤트 (alert firing) | 3d | delete | consumer가 처리 확인 후 RDB 상태 갱신 |
| 동기화 (watchlist, session) | 1d | delete | 로컬 캐시/RDB 동기화 완료 후 재처리 불필요 |

user 토픽을 추가할 때 다음을 확인한다:
- RDB에 이미 영구 저장이 되는가? → 그렇다면 retention을 최소화한다.
- consumer가 장애 후 RDB만으로 상태를 복원할 수 있는가? → 그렇다면 Kafka replay 불필요.
- GDPR/개인정보 규정 대상인가? → retention.ms를 규정 보존 기한 이하로 명시 설정한다.

---

## 7. 변경 시 체크리스트

### retention.ms를 줄일 때

- [ ] 다운스트림 consumer의 최대 허용 다운타임이 새 retention.ms보다 짧은지 확인
- [ ] segment.ms가 새 retention.ms의 절반 이하인지 확인 (세그먼트 롤 지연 포함)
- [ ] retention.bytes와 조합 시 어느 쪽이 먼저 트리거되는지 계산
- [ ] 장애 시나리오 §5를 다시 검토해 영향 구간 변경 여부 명시

### retention.ms를 늘릴 때

- [ ] 브로커 디스크 여유 용량이 충분한지 확인 (retention.ms × 초당 평균 바이트 기준 추정)
- [ ] retention.bytes가 함께 늘어나야 하는지 검토
- [ ] compact 토픽의 경우 retention.ms 추가 시 혼합 정책(`delete,compact`) 동작 방식 이해 필요
- [ ] `*.DLT` 토픽 retention은 장애 분석 SLA 기준으로 별도 협의

### 토픽명 / 파티션 수를 변경할 때

토픽명을 바꾸면 producer, consumer, `NewTopic` Bean, API stream test, coin_front SSE 이벤트명을 함께 갱신해야 한다. 파티션을 늘리면 analytics `PartitionRegistry`의 state 분산 방식이 달라지므로 Redis state key 분포와 replay 동작을 검증한다.
