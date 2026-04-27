# PROJECT CONTEXT - CoinData Platform

> 이 문서를 AI 대화 시작 시 첨부하세요. AI가 프로젝트 구조와 규칙을 이해합니다.

---

## 1. 프로젝트 개요

암호화폐 거래소(Upbit, Binance, Bithumb) 데이터를 수집하여 프리미엄(김프), 캔들, 기술지표를 계산하는 **실시간 데이터 파이프라인** 시스템.

- **언어**: Java 21
- **프레임워크**: Spring Boot 3.4.5
- **빌드**: Gradle 8.4 (멀티모듈)
- **인프라**: Kafka, PostgreSQL (TimescaleDB), Redis
- **직렬화**: JSON (Kafka), Protobuf (contracts 정의), DSL-JSON/Jsoniter (고성능 파싱)

---

## 2. 모듈 구조 및 의존성

```
contracts          ← 모든 모듈이 참조. message record 정의
infra_shard        ← 공통 인프라 (Kafka consumer 베이스, Redis, JSON 유틸)
infra_exchange/    ← 거래소별 API 클라이언트
  ├── binance/
  └── upbit/
infra_heartbeat    ← 모듈간 헬스체크

ingestion/         ← 데이터 수집
  ├── exchange/
  │   ├── ingestion_exchange_shard  ← 공통 (ExchangeStreamManager 등)
  │   ├── binance_ingestion
  │   └── upbit_ingestion
  ├── fx_ingestion                  ← 환율 수집 (네이버)
  └── economic/                     ← 경제지표 수집

meta_data          ← Exchange, MarketCode 마스터 데이터 관리
market_data        ← Tick/Premium/PremiumDetail 계산 및 저장
analytics          ← 캔들/기술지표 계산 (파티션 기반 상태관리)

query/             ← 조회용 (미완성)
  ├── market_data_query
  ├── meta_data_query
  └── analytics_query

trading            ← 트레이딩 (미완성)
```

### 모듈간 의존 방향
```
ingestion → contracts, infra_shard, infra_exchange
meta_data → contracts, infra_shard
market_data → contracts, infra_shard
analytics → contracts, infra_shard
```

---

## 3. 아키텍처 패턴

### Hexagonal Architecture (Port & Adapter)
```
application/
  ├── port/
  │   ├── in/    ← UseCase 인터페이스 (외부→도메인)
  │   └── out/   ← Port 인터페이스 (도메인→외부)
  └── usecase/   ← UseCase 구현체

domain/
  ├── domain/    ← record, enum, 순수 도메인 객체
  ├── service/   ← 도메인 서비스
  ├── buffer/    ← 인메모리 버퍼
  └── store/     ← 상태 저장소 (analytics)

infrastructure/
  ├── messaging/
  │   ├── consumer/   ← Kafka Consumer
  │   ├── publisher/  ← Kafka Producer
  │   └── config/     ← Kafka 설정
  ├── persistence/
  │   ├── entity/     ← JPA Entity
  │   ├── repo/       ← JPA Repository
  │   ├── mapper/     ← Domain ↔ Entity 매퍼
  │   └── adapter/    ← Port 구현 (save/write)
  ├── cache/          ← Redis / 인메모리 캐시
  └── scheduler/      ← @Scheduled 작업
```

---

## 4. 핵심 코딩 컨벤션

### 4.1 타입 파라미터 네이밍
| 파라미터 | 의미 | 예시 |
|----------|------|------|
| `DOMAIN` | 도메인 객체 | `Tick`, `Premium` |
| `MESSAGE` | Kafka 메시지 record | `TickMessage`, `PremiumMessage` |
| `ENTITY` | JPA Entity | `TickEntity` |
| `KEY` | 캐시/맵 키 | `TickKey`, `PremiumKey` |
| `VAL` | 값 객체 | `BigDecimal`, `PremiumDetailValue` |
| `RAW` | 원시 데이터 | `String`, `ByteString` |

### 4.2 매퍼 인터페이스 체계
```java
// infra_shard에 정의된 공통 매퍼
MessageToDomain<MESSAGE, DOMAIN>    // Kafka 메시지 → 도메인
DomainToMessage<DOMAIN, MESSAGE>    // 도메인 → Kafka 메시지
MessageMapping<DOMAIN, MESSAGE>     // 양방향 (위 두개 합침)
DomainToEntity<DOMAIN, ENTITY>      // 도메인 → JPA Entity
EntityToDomain<ENTITY, DOMAIN>      // JPA Entity → 도메인
EntityMapping<DOMAIN, ENTITY>       // 양방향
RawToMessage<RAW, MESSAGE>          // 원시데이터 → 메시지
```

### 4.3 Kafka 토픽 네이밍
```
{소스모듈}.{데이터타입}
예: ingestion-exchange.tick-raw
    meta-data.exchange
    market-data.premium
    analytics.tick-candle
```

### 4.4 Redis 키 네이밍
```
ys:{env}:v1:{도메인}:{용도}:{식별자}
예: ys:local:v1:tick:latest:123
    ys:local:v1:premium:candle:state:0:1m
```

### 4.5 Spring Bean 규칙
- **Port 구현체**: `@Component` 또는 `@Repository`
- **UseCase 구현체**: `@Component` 또는 `@Service`
- **DI**: 생성자 주입만 (`@RequiredArgsConstructor` + `private final`)
- **설정**: `@Configuration` 클래스에 `@Bean` 메서드

### 4.6 도메인 객체 규칙
- 불변 데이터: Java `record` 사용
- 상태 변경 필요 시: `class` + `@Getter` (Setter 최소화)
- sealed interface: `CloseCandle`, `CloseIndicator`, `IndicatorState`, `RecoveryState`

---

## 5. 데이터 흐름

### 5.1 Tick 데이터 파이프라인
```
[Exchange WebSocket] → TickRawHandler → Kafka(tick-raw)
    → TickConsumer(market_data) → ConsumeTickService
        → 캐시 저장 + 버퍼 저장 + Premium 계산 + Kafka(tick) 발행
            → TickConsumer(analytics) → TickAnalyticsService
                → PartitionRegistry → CandleStore/IndicatorStore
```

### 5.2 Premium 계산
```
Tick 수신 → CalPremiumManager
    → 같은 base의 다른 거래소 Tick 조회 (캐시)
    → FX 환율 조회 (캐시)
    → Premium = (comparePrice * compareFx) / (basePrice * baseFx) - 1
    → Premium, PremiumDetail 생성 → 버퍼 + Kafka 발행
```

### 5.3 Analytics 상태관리
```
Kafka Consumer → PartitionRegistry → Store(per partition)
    → 캔들: Interval별 OHLC 버퍼링
    → 지표: Interval별 EMA/RSI/STDDEV/TR/MEAN 계산
    → 스케줄러(cron) → flush → DB 저장 + Kafka 발행
    → 리밸런싱: Redis에 상태 저장/복원
```

---

## 6. 주요 기존 패턴 (새 코드 작성 시 참조)

### Kafka Consumer 패턴
```java
@Component
public class XxxConsumer extends KafkaDomainConsumer<Domain, Message> {
    private final ConsumeXxxUseCase useCase;

    protected XxxConsumer(MessageToDomain<Message, Domain> mapper, ConsumeXxxUseCase useCase) {
        super(mapper);
        this.useCase = useCase;
    }

    @Override
    @KafkaListener(topics = "...", groupId = "...", containerFactory = "...Factory")
    protected void onMessage(ConsumerRecord<String, Message> record) {
        Domain domain = toDomain(parse(record));
        useCase.consume(domain);
    }
}
```

### Batch Save 패턴 (market_data)
```java
@Component
public class PersistXxxBatchUseCase extends PersistPriceValueBatchUseCase<Domain>
        implements FlushPriceValueBufferUseCase.ForXxx {
    public PersistXxxBatchUseCase(WritePriceValuePort<Domain> db,
                                   WriteRedisLatestDataPort<Domain> redis,
                                   XxxBuffer buffer) {
        super(db, redis, buffer);
    }
}
```

### Entity Mapper 패턴
```java
@Component
public class XxxEntityMapper implements EntityMapping<Domain, XxxEntity> {
    @Override
    public XxxEntity toEntity(Domain d) { return XxxEntity.builder()...build(); }
    @Override
    public Domain toDomain(XxxEntity e) { return new Domain(...); }
}
```

### Analytics Store 패턴
```java
public class XxxCandleStore {
    private final Map<Interval, Map<Key, Candle>> buffers;
    public void update(Key key, Value val) { ... }
    public void assign(Map<Interval, List<Candle>> snapshot) { ... }
    public List<Candle> getCandles(Interval interval) { ... }
    public List<CloseCandle> drain(Interval interval) { ... }
}
```

---

## 7. 알려진 제약사항 / 주의점

1. **build.gradle 과잉 의존성**: 대부분 모듈에 불필요한 dependency가 많음 (cassandra, security 등). 새 모듈 생성 시 최소한만 포함할 것
2. **패키지 오타**: `analystics` (analytics 오타), `ingection` (ingestion 오타), `infre_exchange` (infra 오타) — 기존 코드와 일관성 유지 필요
3. **Protobuf vs Java record**: contracts 모듈에 .proto와 Java record 둘 다 존재. 실제 Kafka 직렬화는 JSON(JsonSerializer)을 사용하므로 Java record가 실질적 계약
4. **환경 하드코딩**: `env = "local"`, `bootstrap-servers: localhost:9092` 등이 하드코딩됨
5. **economic 모듈**: 패키지가 `com.example.demo` 없이 루트 패키지로 되어 있음 (빌드 에러 가능)
6. **TickBuffer.flush()**: flush 후 buffer.clear()를 호출하지 않음 (데이터 중복 저장 가능)

## 8. economic 모듈 패키지 컨벤션 (2025 결정)

- Base package: `com.example.demo.ingestion.economic.economic_ind`
- `economic_ind_ingestion_shard`와 `fred_ingestion`은 split-package로 같은 base 공유
  (exchange 모듈의 `ingestion_exchange_shard` ↔ `binance_ingestion`/`upbit_ingestion`과 동일 패턴)
- 디렉토리 경로의 `ingection` 오타는 유지, **클래스 패키지에는 정상 표기 `ingestion` 사용**
- `clinet`/`parer` 오타는 02_PROJECT_CONTEXT.md의 유지 대상 오타가 아님 (별도 PR로 정정)

## 9. Destructive Command Guardrails (필수)

다음 명령들은 사용자 명시적 확인 없이 절대 실행하지 말 것.
실행 의도가 있다면 dry-run을 먼저 보여주고 사용자 확인을 받을 것.

### Always require explicit confirmation
- `git clean` (특히 `-f`, `-d`, `-x` 플래그) — 항상 `git clean -n`으로 먼저 보여줄 것
- `git reset --hard`
- `git checkout -- <path>` (untracked 변경 덮어씀)
- `git stash drop`, `git stash clear`
- `git branch -D`, `git push --force`, `git push --force-with-lease`
- `rm -rf`, PowerShell `Remove-Item -Recurse -Force`
- `./gradlew clean` (build 산출물 외에 의도치 않은 영역 영향 가능)
- 모든 `DROP`, `TRUNCATE`, `DELETE` SQL

### Workflow rules
- mass refactoring (10+ 파일 영향) 시작 전:
    1. `git status`가 clean한지 확인. dirty면 중단
    2. untracked 파일이 있으면 사용자에게 보여주고 처리 방침 확인 (커밋/stash/제외)
    3. 작업 브랜치를 별도로 생성 (`git checkout -b refactor/...`)
- 위 destructive 명령은 세션 단위 always-allow를 절대 받지 말 것
- 명령 실행 전 항상 영향 범위를 사용자에게 보고하고 진행 여부를 묻기