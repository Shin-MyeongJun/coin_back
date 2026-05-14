# Interview Talking Points

> 코드와 문서에 실제로 있는 결정만 적는다. 추측과 일반론은 없다.
> 기준 파일: `CLAUDE.md`, `docs/ARCHITECTURE.md`, `docs/KAFKA_RETENTION_POLICY.md`, `modules/api/_PLAN.md`, 각 모듈 소스.

---

## 1. 프로젝트 한 줄 설명

**CoinData는 Upbit, Binance의 실시간 tick과 환율을 수집해서 김치 프리미엄, candle, indicator를 계산하고 REST API와 SSE로 제공하는 market data pipeline이다.**

- 기술 스택: Java 21, Spring Boot 3.4.5, Kafka, PostgreSQL/TimescaleDB, Redis, Reactor
- 규모: 23개 Gradle 모듈, 13개 주요 Kafka 토픽, 5단계 데이터 파이프라인
- 흐름 요약: `거래소 WebSocket → ingestion → Kafka → market_data → analytics → query → api REST/SSE`

---

## 2. 핵심 의사결정 5선

### 결정 1. 멀티모듈 write-side / read-side 완전 분리

**결정**
write-side(`market_data`, `analytics`, `meta_data`), read-side(`query/*` 4종), 진입점(`api`) 모듈을 분리했다. `api` 모듈은 `query/*`, `contracts`, `infra_shard`, `infra_heartbeat`만 의존한다. write-side 모듈 직접 의존은 금지했다.

**대안**
단일 모듈에 write/read 패키지만 나누거나, write-side 모듈에 query 패키지를 함께 두는 방식.

**선택 근거**
write-side DB 스키마나 Kafka 메시지 구조가 바뀌어도 query/api 레이어가 연쇄적으로 영향을 받지 않게 하려고 선택했다. `api` 모듈이 `market_data` 내부에 직접 접근하면, 도메인 로직과 API 로직 사이의 경계가 흐려진다. `_PLAN.md §공통 결정사항`에 "도메인 모듈 직접 의존 금지, ArchUnit 가드 권장"으로 명시했다.

**트레이드오프**
동일 도메인 개념(예: Tick)이 write-side의 `domain/domain/Tick.java`와 read-side의 View record로 별도 정의된다. 23개 모듈의 초기 설정 비용과 빌드 의존성 관리 복잡도가 있다. ArchUnit 가드는 권장만 되어 있고 실제 테스트 파일은 아직 없다.

**현 시점 평가**
같은 선택을 다시 할 것이다. 다만 23개 모듈은 팀 규모가 작을 때 오버헤드가 크다. 5~6개 핵심 모듈에서 시작해서 검증 후 분리하는 방식이 더 나았을 수도 있다.

> 근거: `settings.gradle`, `modules/api/_PLAN.md §공통 결정사항`

---

### 결정 2. Analytics: Kafka partition lifecycle + Redis state 복원

**결정**
`PartitionLifecycle` 인터페이스(`assignPartition(int)` / `revokePartitions(Collection<Integer>)`)를 정의하고, `TickPartitionRegistry`가 `ConcurrentHashMap<Integer, TickCandleStore>` / `ConcurrentHashMap<Integer, TickIndicatorStore>`를 파티션 단위로 유지한다. Kafka rebalance 시 `restoreCandles()` / `restoreIndicators()`가 Redis snapshot을 store에 주입해 candle/indicator 상태를 복원한다.

**대안**
- Kafka earliest offset부터 replay해서 state를 재구성. analytics.* 토픽 24h retention이므로 기술적으로 가능하다.
- rebalance 후 상태 없이 새 bucket부터 계산 시작 — 이전 bucket은 공백.

**선택 근거**
Kafka replay는 재구성 시간이 O(토픽에 쌓인 메시지 수)다. Redis에서 복원하면 O(파티션 수)다. rebalance가 자주 일어나는 환경에서 Kafka replay는 analytics 처리 공백이 길어진다. PartitionLifecycle을 인터페이스로 추출하면 TickPartitionRegistry, PremiumPartitionRegistry, PremiumDetailPartitionRegistry가 동일한 계약을 구현하므로 Kafka consumer listener 코드가 Registry 타입에 독립적이다.

**트레이드오프**
Redis가 다운되면 state recovery 경로가 없다. Kafka replay fallback은 구현되어 있지 않다. Redis key 구조와 PartitionRegistry 내부가 강하게 결합되어 있어 Redis 스키마 변경이 어렵다.

**현 시점 평가**
방향은 맞지만, Redis 단일 장애점 문제는 인지하고 있다. 향후 Redis가 없어도 analytics.* 24h Kafka replay로 복원하는 fallback 경로를 추가해야 한다.

> 근거: `modules/analytics/.../domain/partition_registry/PartitionLifecycle.java`, `TickPartitionRegistry.java`, `TickCandleStore.java`

---

### 결정 3. SSE fanout: Reactor Sinks.Many + UUID consumer group

**결정**
`MarketDataStream`, `AnalyticsStream`이 `Sinks.many().multicast().onBackpressureBuffer()`를 보유한다. Kafka consumer는 Sink에 emit하고, SSE handler는 Sink를 `asFlux()`로 subscribe한다. `StreamKafkaConfig`에서 `"api.stream." + UUID.randomUUID()`로 인스턴스마다 고유 group-id를 생성해 전체 메시지 수신(fanout)을 보장한다. emitter는 `onCompletion` / `onTimeout` / `onError` 3곳 모두에 `Disposable.dispose()`를 등록한다.

**대안**
- SSE controller 안에 `@KafkaListener`를 직접 선언. 구현이 단순하지만 WebSocket을 추가할 때 consumer 코드를 복제해야 한다.
- WebFlux Reactive Kafka. api 모듈이 MVC 기반이라 혼용 복잡도가 높다.

**선택 근거**
`_PLAN.md §안전장치 2`에 명시한 대로, Kafka consumer와 SSE delivery를 Sink로 분리하면 나중에 WebSocket을 추가할 때 같은 Sink를 subscribe하기만 하면 된다. `[Kafka Consumer] → [Sinks.Many<Domain>] → [SSE Emitter]` / `→ [WebSocketHandler]` 구조가 설계 시점에 문서화됐다. Sink 타입이 도메인 메시지(`Sinks.Many<TickMessage>`)이고 직렬화는 각 핸들러가 담당하므로 채널이 늘어도 consumer 수정이 없다.

**트레이드오프**
인스턴스가 N개면 Kafka consumer가 N개 생긴다. 토픽의 파티션 수가 많고 인스턴스가 늘면 Kafka 연결 수가 비례해서 증가한다. `_PLAN.md`에서 멀티 인스턴스 스케일아웃 시 Redis Pub/Sub 브릿지로 전환 검토를 명시했지만 아직 미구현이다.

**현 시점 평가**
단일 인스턴스에서는 동작한다. 스케일아웃 요건이 생기면 Redis Pub/Sub 전환이 필요하다. 설계 시점에 확장 지점을 문서화한 것은 유효하다.

> 근거: `modules/api/.../stream/config/StreamKafkaConfig.java`, `MarketDataStream.java`, `TickSseHandler.java`, `modules/api/_PLAN.md §안전장치 2`

---

### 결정 4. contracts 모듈 분리 + Java record vs proto

**결정**
`contracts` 모듈을 별도 Gradle 모듈로 분리하고, 런타임 Kafka 메시지 계약을 Java record + Jackson JSON으로 채택했다. 같은 모듈에 `.proto` 파일도 존재하지만 런타임에서는 사용하지 않는다.

**계층별 타입 결정**
- `TickMessage`: `BigDecimal bid, ask` — 도메인 값을 그대로 유지
- `TickCandleMessage`: OHLC 4개 필드를 `String` — Kafka 메시지 레이어에서 소수점 표현을 String으로 고정

**대안**
- proto + schema registry로 전체 직렬화 통일 — 계약 버전 관리가 명시적이고 다언어 클라이언트를 지원한다.
- contracts 모듈 없이 각 모듈이 자체 DTO 정의 — 모듈 독립성은 높아지지만 계약 변경이 암묵적으로 퍼진다.

**선택 근거**
현재 모든 consumer가 JVM 기반이고, Jackson이 Java record를 별도 codegen 없이 바로 처리한다. proto는 IDE 플러그인, codegen 설정, schema registry 인프라가 추가로 필요해서 MVP 속도를 늦춘다. CLAUDE.md §13.2에서 proto를 "향후 gRPC, 다언어 클라이언트 확장 후보"로 명시했다.

`TickCandleMessage`에서 OHLC를 String으로 결정한 이유는 JSON 직렬화 시 BigDecimal이 지수 표기법(`1E+10`)으로 출력되는 케이스를 방지하고 소수점 자리를 직렬화 시점에 고정하기 위해서다.

**트레이드오프**
Java record 변경 시 producer와 consumer 사이 계약 불일치를 컴파일 타임에 잡지 못한다. 런타임에서야 역직렬화 실패로 드러난다. 향후 다언어 클라이언트가 생기면 proto 전환 비용이 발생한다.

**현 시점 평가**
MVP 범위에서 맞는 선택이었다. 다언어 클라이언트나 schema registry 요건이 생기면 proto 전환 비용을 들여야 한다.

> 근거: `modules/contracts/src/main/java/.../message/price_value/TickMessage.java`, `TickCandleMessage.java`, `CLAUDE.md §13.2`

---

### 결정 5. BigDecimal 도메인 + sealed CloseCandle + Kafka retention 도메인별 차등

**결정 (3가지를 묶은 이유: 모두 "경계에서 타입 안전성"을 다루는 결정이다)**

**BigDecimal 도메인**: `Tick`, `Premium`, `PremiumDetail` 도메인 record가 모두 `BigDecimal bid, ask`를 사용한다. premium 계산에서 `scale=8, RoundingMode.HALF_UP`을 명시했다. EMA indicator 계산에서 `MathContext(12, RoundingMode.HALF_UP)`을 명시했다.

**sealed CloseCandle**: `sealed interface CloseCandle permits TickCloseCandle, PremiumCloseCandle, PremiumDetailCloseCandle`로 analytics에서 닫힌 candle의 허용 타입을 컴파일 타임에 제한했다. `TickCloseCandle`은 record로 정의해 불변이다.

**Kafka retention 도메인별 차등**: raw 토픽 1h, market-data 3~6h, analytics 24h, economic 7d로 다운스트림 복구 윈도우 기준으로 retention을 차등 적용했다. retention.ms만이 아니라 retention.bytes, segment.ms도 함께 설정해서 트래픽 급증 시 디스크 보호와 세그먼트 롤링 지연을 제어한다.

**대안**
- double 또는 long(소수점 이하 고정 승수)으로 금융값 저장
- CloseCandle을 일반 interface로
- 모든 토픽에 동일한 retention 정책 적용

**선택 근거**
BigDecimal은 IEEE 754 double의 이진 표현 오차를 피한다. premium 공식 `(bidB / askA - 1) * 100`에서 소수점 8자리 이하 차이가 순위에 영향을 준다. sealed interface는 `when` 표현식에서 exhaustiveness를 컴파일러가 보장한다. 새로운 candle 타입이 추가되면 `permits` 목록 변경 없이 컴파일이 실패해서 누락을 잡아준다. Kafka retention 차등은 "retention = 다운스트림이 장애로부터 복구를 시도하는 데 허용할 수 있는 최대 시간"이라는 원칙에서 도출했다.

**트레이드오프**
BigDecimal은 double 대비 연산 비용이 있다. 지표를 수천 개 파티션에 동시 계산할 때 이 비용이 누적된다. `TickCandleMessage`의 OHLC가 String이면 downstream에서 정렬/비교를 하려면 파싱이 필요하다. 실측 throughput은 없고 로컬 smoke test 통과 수준만 확인했다.

**현 시점 평가**
도메인 레이어에서 BigDecimal이 맞다. Kafka retention 차등은 첫 번째로 적용한 운영 정책이므로, 실제 트래픽 데이터가 쌓이면 bytes 설정을 조정해야 할 수 있다.

> 근거: `market_data/.../domain/domain/Tick.java`, `Premium.java`, `CalPremiumManager.java`, `analytics/.../CloseCandle.java`, `TickCloseCandle.java`, `EmaUpdater.java`, `docs/KAFKA_RETENTION_POLICY.md`

---

## 3. 자주 받을 질문 Q&A

### 기술 깊이 질문

**Q: Kafka consumer rebalance 시 candle state는 어떻게 복구되나요?**

결론: `PartitionLifecycle.assignPartition(int)`가 호출되면 Redis에서 state를 읽어 `TickCandleStore.assign()`으로 주입한다.

rebalance 이후 새 파티션이 할당되면 Kafka listener가 `assignPartition(partitionId)`를 호출한다. `TickPartitionRegistry`가 해당 파티션 ID에 대한 `TickCandleStore`와 `TickIndicatorStore`를 새로 생성하거나, Redis에서 읽어온 `Map<Interval, List<TickCandle>>` snapshot을 `store.assign()`으로 주입한다. `revokePartitions()`가 먼저 호출돼 해당 파티션의 store를 `ConcurrentHashMap`에서 제거한다. store가 없는 파티션으로 update가 들어오면 `log.warn("Partition {} has no store. Skipping update.")`를 남기고 무시한다.

> `modules/analytics/.../domain/partition_registry/TickPartitionRegistry.java`, `TickCandleStore.java`

---

**Q: 같은 tick이 시스템 안에서 몇 번 직렬화되나요? 그 비용을 어떻게 정당화했나요?**

결론: WebSocket 수신부터 analytics까지 최소 3~4회 직렬화·역직렬화가 일어난다.

경로: `거래소 WebSocket raw JSON → TickRawMessage(Kafka JSON) → TickMessage(Kafka JSON) → Tick domain → TickEntity(JPA) → TickMessage(Kafka JSON) → analytics에서 다시 역직렬화`. 각 변환은 `infra_shard`의 `MessageToDomain`, `DomainToEntity`, `DomainToMessage` 인터페이스가 담당한다. 직렬화 비용을 실측한 적은 없다. 설계의 의도는 각 계층 경계를 명확히 해서 Kafka 메시지 schema나 DB entity가 바뀌어도 domain 객체가 영향을 받지 않도록 하는 것이다. 직렬화 비용이 실제 문제로 드러나면 domain → entity 직접 변환 경로 단축을 검토할 수 있다.

> `modules/infra_shard/.../messaging/mapper/` (8개 인터페이스)

---

**Q: BigDecimal 누적 계산의 정밀도 문제는 어떻게 다뤘나요?**

결론: 도메인 레이어에서 BigDecimal을 쓰고, 연산에 `MathContext`와 `RoundingMode`를 명시했다. Kafka 메시지 레이어에서는 String으로 변환해 직렬화 오차를 차단했다.

`EmaUpdater`는 `MathContext(12, RoundingMode.HALF_UP)`을 상수로 고정하고 모든 연산에 적용한다. premium 계산 `CalPremiumManager.makePremium()`은 `divide(askA, 8, RoundingMode.HALF_UP)`로 scale을 명시한다. `TickCandleMessage`의 OHLC 4개 필드는 String 타입이다. RsiState, StddevState 등 다른 indicator updater의 MathContext 설정은 동일한 패턴을 사용하는지 모든 updater를 점검하지는 않았다.

> `modules/analytics/.../indicator/open/updater/EmaUpdater.java`, `modules/market_data/.../CalPremiumManager.java`, `modules/contracts/.../candle/TickCandleMessage.java`

---

**Q: TickBuffer가 동시성 환경에서 안전한 이유는?**

결론: `ConcurrentHashMap`과 `flush()`의 snapshot → clear 순서 조합으로 race window를 최소화했다.

`buffer = new ConcurrentHashMap<>()`이므로 `add()`는 thread-safe하다. `flush()`는 `List.copyOf(buffer.values())`로 snapshot을 먼저 만들고 `buffer.clear()`를 호출한다. snapshot과 clear 사이에 들어온 새 데이터는 다음 flush에 포함된다. 단, `List.copyOf()` 이후 `buffer.clear()` 이전에 쓴 데이터는 snapshot에 없고 clear로 사라지는 race window가 남아 있다. 이 손실은 설계상 수용하는 범위다. 스케줄러가 flush를 단일 스레드로 호출하면 이 window가 최소화된다.

> `modules/market_data/.../domain/buffer/TickBuffer.java`

---

**Q: SSE 다중 인스턴스 환경에서 fanout이 어떻게 보장되나요?**

결론: 현재는 인스턴스마다 UUID group-id로 Kafka 전체 메시지를 수신한다. 다중 인스턴스에서는 인스턴스당 독립 consumer가 생긴다.

`StreamKafkaConfig`에서 `GROUP_ID_CONFIG = "api.stream." + UUID.randomUUID()`를 설정한다. 각 인스턴스가 독립 consumer group이므로 같은 토픽 메시지를 모두 수신한다. 인스턴스가 늘면 Kafka consumer 연결 수가 비례해서 늘어난다. `api/_PLAN.md`에서 "멀티 인스턴스 스케일아웃 시 Redis Pub/Sub 브릿지로 전환 검토"를 명시했지만 아직 구현하지 않았다.

> `modules/api/.../stream/config/StreamKafkaConfig.java`, `modules/api/_PLAN.md`

---

**Q: 김프(premium) 계산 공식과 환율 시점 일관성은 어떻게 처리했나요?**

결론: tick 수신 시점에 FX cache에서 환율을 조회해서 `System.currentTimeMillis()`를 기준 타임스탬프로 삼는다.

`CalPremiumManager.cal(Long id)`는 tick이 들어오면 동일 base symbol의 다른 거래소 tick을 cache에서 가져온다. 각 거래소의 quote 통화(`KRW`, `USD`, `JPY`)를 `normalize()`로 정규화하고, `fxGetter.get(new FxKey(quoteA, quoteB))`로 FX cache를 조회한다. FX는 `fx_ingestion`이 1초 주기로 Naver API를 호출해 cache를 갱신한다. 따라서 FX 시점 오차는 최대 1초다. `filterling(quoteA, quoteB)`는 동일 통화 쌍(예: KRW-KRW)에서 계산을 건너뛴다. premium 공식: `bid = (bidB * fxB) / (askA * fxA) - 1`, 단위 %.

> `modules/market_data/.../application/usecase/CalPremiumManager.java`, `modules/ingection/fx_ingestion/`

---

**Q: contracts 모듈에 proto가 있는데 왜 안 쓰나요?**

결론: 현재는 JVM 단일 환경이고 Java record + Jackson으로 충분해서 proto codegen 비용을 피했다.

proto는 schema registry, IDE 플러그인, `protoc` 빌드 설정이 추가로 필요하다. 현재 모든 producer와 consumer가 Java이고 다언어 클라이언트 요건이 없다. `CLAUDE.md §13.2`에서 "proto는 gRPC, schema registry, 다언어 클라이언트 확장 후보"로 명시했다. Java record 변경이 producer/consumer 계약 불일치를 런타임에야 드러낸다는 위험은 인지하고 있다.

> `modules/contracts/`, `CLAUDE.md §13.2`

---

**Q: hexagonal architecture로 얻은 게 구체적으로 무엇인가요?**

결론: Kafka와 JPA 의존이 domain/usecase 레이어 밖으로 밀려나서, domain 로직 테스트에 Kafka나 DB가 필요 없다.

`market_data`의 `ConsumeTickService`, `PublishPremiumService`는 Port 인터페이스만 의존한다. 테스트에서 BDDMockito로 Port를 mocking하고 domain 로직만 검증한다. `TickPartitionRegistry`는 `@SpringBootTest` 없이 순수 단위 테스트로 동작한다. 실제 테스트 파일 수: `market_data` 37개, `analytics` 18개, `api` 11개. mapper 단위 테스트(`TickMessageMapperTest`, `TickEntityMapperTest` 등)가 계층 경계 변환을 별도로 검증한다.

> `modules/market_data/src/test/`, `modules/analytics/src/test/`

---

### 운영 질문

**Q: 데이터 폭증 문제를 어떻게 해결했나요?**

결론: retention.ms 외에 retention.bytes와 segment.ms를 함께 설정했다. lz4 압축을 producer 전체에 적용했다.

`market-data.tick`은 `retention.ms=6h`, `retention.bytes=30GB`, `segment.ms=1h`다. 트래픽이 급증하면 retention.ms에 도달하기 전에 retention.bytes가 먼저 트리거되어 디스크를 보호한다. segment.ms는 트래픽이 없어도 세그먼트를 롤링해서 retention.ms와의 조합 지연을 제한한다. lz4는 JSON 텍스트 payload에서 압축 효율이 높고 CPU 부하가 낮다. retention.bytes 수치는 합성 트래픽 기준 추정값이므로 실제 트래픽이 쌓이면 조정이 필요하다.

> `docs/KAFKA_RETENTION_POLICY.md`, 각 모듈 `*KafkaAdminConfig.java`, `*KafkaProducerConfig.java`

---

**Q: 로컬에서 실운영 중 어떤 장애를 만났나요?**

결론: premium 미생성과 analytics candle 미생성을 직접 디버깅했다.

premium 미생성은 두 원인이 있었다. FX cache가 없어서 `fxGetter.get()`이 empty를 반환하거나, 양쪽 거래소 tick 입력 순서 때문에 비교 대상 tick이 없는 경우였다. `DEMO.md §Troubleshooting`에 "양쪽 tick 입력 후 기준 거래소 tick을 한 번 더 넣어 트리거"라는 가이드가 남아 있다. analytics candle 미생성은 partition store가 준비되기 전에 update가 들어온 경우였다. `TickPartitionRegistry.update()`에서 `log.warn("Partition {} has no store. Skipping update.")`로 확인했다.

> `modules/market_data/.../CalPremiumManager.java`, `modules/analytics/.../TickPartitionRegistry.java`, `docs/DEMO.md §Troubleshooting`

---

**Q: 관측성은 어떻게 구성했나요?**

결론: Prometheus + Grafana + kafka-exporter + redis-exporter + Spring Actuator를 Docker Compose overlay로 구성했다.

`docker/docker-compose.observability.yml`을 base Compose에 overlay해서 실행한다. Grafana에 `CoinData Infra Overview` 대시보드를 자동 프로비저닝하고 kafka-exporter로 consumer lag, redis-exporter로 Redis memory/key count/latency를 수집한다. api 모듈의 JVM, HTTP, application 메트릭은 `localhost:8080/actuator/prometheus`로 scrape한다. `infra_heartbeat` 모듈별 health gauge 패널은 아직 없다.

> `docker/docker-compose.observability.yml`, `docs/observability.md`

---

**Q: 배포는 어떻게 했나요?**

결론: 로컬 Docker Compose로 인프라를 띄우고 Gradle bootRun으로 모듈을 실행했다. 클라우드 배포 경험은 없다.

PowerShell 스크립트(`scripts/run/start-runtime.ps1`)로 모듈 실행 순서를 관리한다. 실제 거래소 API 키를 연결해서 Upbit/Binance 실시간 tick 수신, premium/candle/indicator 생성까지 로컬에서 확인했다. CI/CD 파이프라인은 구성하지 않았다.

---

### 설계 비판 질문

**Q: 모듈을 왜 이렇게 많이 쪼갰나요? 오버엔지니어링 아닌가요?**

결론: write-side와 read-side가 같은 모듈에 있으면 api 레이어가 write-side 내부에 의존하게 된다. 이걸 막으려고 분리했다.

`query/*`가 write-side에 의존하지 않으면, `market_data` DB 스키마를 바꿔도 `api` 수정이 연쇄되지 않는다. `ingestion` 모듈 분리로 수집기 장애가 `market_data` 모듈 재기동에 영향을 주지 않는다. 다만 23개 모듈은 팀이 혼자일 때 초기 설정 비용이 크고, 같은 개념(Tick)을 도메인 레이어와 View record에 두 번 정의해야 하는 반복이 있다.

---

**Q: 패키지명에 오타가 있던데(`analystics`, `ingection`)?**

결론: 의도적으로 유지하고 있다. 기존 코드와 import에 이미 녹아 있어서 rename은 10개 이상 파일에 영향을 주는 별도 작업이다.

`CLAUDE.md §8`에 기존 오타 목록과 유지 사유가 명시되어 있다. 기능 변경에 rename을 섞으면 diff 추적이 어렵다. 별도 PR로 진행할 예정이다.

---

**Q: trading 모듈은 왜 비어있나요?**

결론: MVP 범위 밖으로 명시적으로 제외했다. 자동매매, 주문 실행, 포지션 관리는 market data pipeline 검증 후 다음 단계다.

`CLAUDE.md §13.1`에 "trading은 확장 후보, MVP 제외 항목"으로 명문화했다. Gradle 모듈과 build.gradle은 있고 Java 소스는 없다. 이번 MVP의 기능 누락이 아니라 범위 관리다.

---

**Q: 프론트엔드는 왜 별도 프로젝트인가요?**

결론: 이 저장소의 범위는 REST/SSE API 제공까지다. 화면은 `coin_front` 프로젝트가 담당한다.

`coin_front`는 Next.js 15 / React 19 스택이다. API 응답 컨벤션(epoch ms, RFC 7807, cursor 페이징)은 양쪽 팀이 합의해서 `CLAUDE.md §14`에 고정했다. 화면이 없는 것은 이 저장소의 결함이 아니다.

---

**Q: 실제 운영해본 적이 있나요?**

결론: 로컬 Docker 환경에서 실제 거래소 API 키를 연결해 전체 파이프라인을 검증했다. 클라우드 운영은 하지 않았다.

Upbit, Binance 실시간 tick 수신, premium/candle/indicator 생성, Redis latest key, API SSE까지 로컬에서 확인했다. `scripts/e2e/stream-smoke.ps1`로 Kafka publish → SSE 수신 경로를 반복 검증했다. 클라우드(EKS, GKE 등) 배포와 실제 트래픽 부하 측정 경험은 없다.

> `docs/DEMO.md`, `README.md §검증된 범위`

---

## 4. 명시적으로 안 한 것

MVP에서 의도적으로 제외한 항목이다. 기능 누락이 아니라 범위 관리다.

| 항목 | 제외 이유 |
|---|---|
| `trading` 모듈 (자동매매, 주문, 포지션, 리스크 관리) | market data pipeline 검증 후 단계. 주문 실행은 별도 거래소 private API 인증과 리스크 제어가 필요해서 MVP에 섞지 않았다. |
| proto/gRPC/schema registry | JVM 단일 환경에서 Java record + JSON으로 충분. 다언어 클라이언트 요건 발생 시 확장 후보. |
| 경제지표 실시간 downstream | 경제지표 수집·저장·REST 조회는 있다. analytics/SSE 연결은 없다. 경제지표 발표 주기가 market data보다 길어 MVP 실시간성 데모 범위 밖으로 판단했다. |
| 프론트엔드 대시보드 | `coin_front` 별도 프로젝트에서 담당. API 컨벤션 합의는 완료됐다. |
| 인증(JWT)/API Key/알람/워치리스트 백엔드 모듈 | `SecurityFilterChain`이 `permitAll()` 상태. 외부 클라이언트 단계 진입 시 추가. `CLAUDE.md §14.3`에 백엔드 작업 대기 항목으로 정리했다. |
| CI/CD 파이프라인 | 미구성. 로컬 Gradle 검증만. |
| query 모듈 SQL 실행 통합 테스트 | Testcontainers 기반 실제 PostgreSQL 실행 검증이 없다. `CLAUDE.md §9`에 별도 보강 후보로 명시했다. |

---

## 5. 약점과 그 답변

**클라우드 운영 경험 없음**
로컬 Docker로 전체 파이프라인을 검증했다. Kafka, Redis, PostgreSQL, Prometheus/Grafana를 Docker Compose로 띄우고 실제 거래소 API와 연결해서 확인했다. 클라우드(EKS, GKE) 배포는 아직이다.

**대규모 트래픽 실측 없음**
합성 데이터와 smoke test로 검증했다. retention.bytes 수치(예: market-data.tick 30GB)는 예상 트래픽 기반 추정값이다. 실제 트래픽이 쌓이면 조정이 필요하다. 실측 throughput, latency 수치를 이 자리에서 말하지 않는다.

**패키지 오타 다수** (`analystics`, `ingection`, `infre_exchange` 등)
의도적으로 유지하고 있다. 기존 코드에 녹아 있어서 rename은 별도 PR로 분리해서 진행해야 한다. 기능 작업에 섞으면 diff가 뒤섞인다. `CLAUDE.md §8`에 목록과 사유가 있다.

**query 모듈 SQL 실행 통합 테스트 갭**
`market_data_query`, `meta_data_query`, `analytics_query`, `economic_query` 4종에 SQL 실행 검증이 없다. mapper 단위 테스트와 파라미터 단위 테스트는 있다. Testcontainers 기반 실제 PostgreSQL 실행 검증을 추가해야 한다.

**CI/CD 미구성**
GitHub Actions로 컴파일/단위 테스트 자동화가 필요하다는 걸 인지하고 있다. 현재는 로컬에서 `.\gradlew.bat compileJava`, `compileTestJava`를 수동으로 돌린다.

**인증/인가 미완성**
`SecurityFilterChain`이 모든 요청을 `permitAll()`로 허용한다. 외부 클라이언트 단계에서 API Key, JWT를 추가할 계획이다. SSE endpoint에서 URL 쿼리 토큰 방식을 쓸지는 `CLAUDE.md §14.3`에 논의 항목으로 남아 있다.

**Redis 단일 장애점 (analytics state)**
analytics partition state 복원이 Redis에만 의존한다. Redis가 다운되면 rebalance 시 state를 복원할 방법이 없다. Kafka replay fallback(analytics.* 24h retention 활용)은 구현되어 있지 않다.

---

## 6. 한 번 더 강조할 것

면접 마무리에 꺼낼 만한 차별점 3가지다.

**① Kafka partition lifecycle 기반 analytics state 복원**
candle/indicator 계산 상태를 Kafka rebalance 후에도 유실하지 않는 구조다. `PartitionLifecycle` 인터페이스로 `assignPartition`/`revokePartitions`를 명시하고, `ConcurrentHashMap<Integer, TickCandleStore>`로 파티션을 격리한다. Redis snapshot을 `store.assign()`으로 주입해서 복원한다. "Kafka consumer rebalance 시 상태를 어떻게 다루나"를 설계 시점에 인터페이스로 정의한 것이 핵심이다.

> `modules/analytics/.../domain/partition_registry/`, `TickCandleStore.java`

**② write-side / read-side 완전 분리 + api 의존성 제한**
`query/*` 4종이 write-side 모듈에 역참조가 없다. `api` 모듈은 `query/*`, `contracts`, `infra_shard`만 볼 수 있다. 이 제약을 `_PLAN.md`에 명시했고 ArchUnit 가드를 권장했다. 모듈이 23개인 이유가 오버엔지니어링이 아니라 이 의존성 방향을 강제하려는 선택이다.

> `settings.gradle`, `modules/api/_PLAN.md §공통 결정사항`

**③ SSE Sink 격리 설계 — 확장 포인트를 설계 시점에 문서화**
Kafka consumer와 SSE delivery를 `Sinks.Many<T>`로 분리해서 WebSocket 추가 시 consumer 수정이 없는 구조를 설계 시점에 확정했다. `_PLAN.md §안전장치 2`에 `[Kafka Consumer] → [Sinks.Many<Domain>] → [SSE Emitter]` / `→ [WebSocketHandler]` 구조로 명시했다. Disposable 관리를 onCompletion / onTimeout / onError 3곳에 등록해서 emitter 누수를 막았다.

> `modules/api/.../stream/sink/MarketDataStream.java`, `TickSseHandler.java`, `modules/api/_PLAN.md`

---

## 7. 답변 시 주의

- 모르는 건 모른다고 한다. "이 부분은 검토만 하고 구현은 안 했습니다"가 정직한 답변이다.
- 숫자를 정확히 외우려 하지 않는다. "대략 20여 개 모듈", "analytics는 24시간 retention" 정도가 자연스럽다. 틀린 숫자를 자신 있게 말하는 것보다 범위로 말하는 것이 낫다.
- 실측하지 않은 throughput, latency 수치를 말하지 않는다. "로컬 smoke test 통과 수준만 확인했다"로 끊는다.
- 회사 기술 스택과 비교해서 단정하지 않는다. "이 프로젝트에서는 X를 선택했고 이유는 Y였다"로 말한다.
- 약점 섹션의 항목을 질문받으면 변명하지 않고 인정 + 향후 계획으로 끊는다.
