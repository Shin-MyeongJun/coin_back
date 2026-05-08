# 01 — market_data 테스트 프롬프트

> **선행 첨부 필수**: `00_TEST_COMMON_BASE.md`
> 본 파일은 **market_data 모듈 한정 추가 지시**만 담는다.

---

## 작업 대상

`modules/market_data/` 전체.

핵심 도메인:
- `Tick`, `Premium`, `PremiumDetail`, `Fx`, `PriceValue`
- 스냅샷: `MarketCodeSnapShotVal`, `ExchangeSnapShotVal`
- 키: `TickKey`, `PremiumKey`, `FxKey`

---

## 이 모듈에서 가장 중요한 테스트 대상 (반드시 커버)

### 1. `CalPremiumManager` (최우선)

이 모듈의 비즈니스 핵심. 협력자가 7개로 가장 복잡하다.

테스트해야 할 시나리오 (모두 단위 테스트, 모든 Port mock):
- `cal(id)` 호출 시 같은 `baseAsset`으로 묶인 다른 거래소의 Tick과 페어링되는지
- `marketCodeList`(ConcurrentHashMap) 가 base별로 marketCodeId를 누적하는지
- `codeGetter` / `exchangeGetter` / `tickGetter` / `fxGetter` 중 하나라도 `Optional.empty()`면 조기 종료하는지 (각 분기마다 별도 케이스)
- `quoteA == quoteB` 인 경우 fxB = 1로 처리되는지
- Premium 계산 공식: `(comparePrice * compareFx) / (basePrice * baseFx) - 1`
  - bid/ask 각각 정확히 곱셈/나눗셈 적용되는지 (BigDecimal 스케일/반올림 검증)
- `buffer.add(premium)` / `detailBuffer.add(premiumDetail)` / `premiumPublisher.process(...)` / `premiumDetailPublisher.process(...)` 가 **모두 호출되는지** (`ArgumentCaptor`로 인자 캡처)
- 동일 id 자기 자신은 페어링에서 제외되는지 (`code.equals(id)` continue)
- 동시성: 같은 base로 여러 스레드가 `cal()` 호출 시 `marketCodeList` 가 일관성 유지하는지 (CountDownLatch + ExecutorService)

### 2. Buffer 계열 (`PriceValueBuffer<DOMAIN>` 구현체)

- `add()` 후 `flush()` 시 모든 항목이 반환되는지
- **`TickBuffer.flush()`는 알려진 버그(02_PROJECT_CONTEXT.md §7-6): flush 후 buffer.clear()를 호출하지 않음**
  - 테스트는 **현재 동작을 그대로 검증**하는 케이스 + 별도 `@Disabled` 또는 `@Tag("known-bug")` 로 "버그 재현" 케이스를 둔다
  - 프로덕션 코드 수정 금지. "검증 포인트" 섹션 끝에 별도로 "기존 버그 재현 테스트가 포함됨"이라고 명기
- 동시 add + flush 동시성 (atomicity)

### 3. Batch Save 패턴 (`PersistPriceValueBatchUseCase<DOMAIN>` 파생 클래스들)

- `WritePriceValuePort<DOMAIN>` (DB), `WriteRedisLatestDataPort<DOMAIN>` (Redis), `XxxBuffer` 협력자
- `flush()` 시 buffer drain → DB write → Redis write 순서 검증 (`InOrder` mock 사용)
- DB 실패 시 Redis 호출되지 않는지 (정책 확인 후 케이스 분기)
- 빈 버퍼일 때 no-op 인지

### 4. 캐시 어댑터 (`GetCacheDataPort<KEY, VAL>` 구현체)

- `get(key)` Optional 반환 — Redis 미스 시 `Optional.empty()`
- 직렬화 라운드트립 (특히 BigDecimal — 문자열 직렬화 권장)
- `RedisKeys` 유틸이 만든 키와 실제 Redis에 저장된 키가 정확히 일치 (Testcontainers Redis로 직접 조회)
- TTL 적용 여부

### 5. Kafka Publisher (`PublishPriceDataUseCase<DOMAIN>` 구현체)

- 토픽명이 정확히 `market-data.tick`, `market-data.premium`, `market-data.premium-detail` 인지 (상수 참조)
- 메시지 key가 `marketCodeId` 또는 합성 키인지
- Testcontainers Kafka에 publish → 별도 KafkaConsumer로 수신 → payload 동치 검증

### 6. Kafka Consumer (`TickConsumer` 등)

- `KafkaDomainConsumer<DOMAIN, MESSAGE>` 베이스 패턴
- `MessageToDomain` 매퍼가 호출되고, 결과 도메인이 UseCase로 전달되는지
- 직렬화 실패 메시지(잘못된 JSON) 처리 (DLT 정책 확인 후 검증)

### 7. `PriceValueConverter`

- 단순 변환이지만 3가지 오버로드 (Tick/Premium/PremiumDetail) 각각 단위 테스트
- 필드 매핑: `bid`, `ask`, `timestamp` 정확히

### 8. JPA Repository / Entity Mapper

- `EntityMapping<Tick, TickEntity>` 등 양방향 매퍼 — 모든 필드 라운드트립
- `@DataJpaTest` + Testcontainers Postgres
- TimescaleDB 하이퍼테이블 의존 쿼리가 있다면 `timescale/timescaledb:latest-pg16` 이미지 사용 명시

---

## 토픽 / Redis 키 검증 (이 모듈에서 사용하는 것)

| 종류 | 패턴 | 검증 위치 |
|---|---|---|
| Kafka 토픽 | `market-data.tick`, `market-data.premium`, `market-data.premium-detail` | Publisher 테스트, Consumer 테스트 |
| Kafka 토픽 (입력) | `ingestion-exchange.tick-raw` (consume) | TickConsumer 테스트 |
| Redis 키 | `ys:{env}:v1:tick:latest:{marketCodeId}` | Cache adapter 테스트 |
| Redis 키 | `ys:{env}:v1:fx:latest:{base}:{quote}` | Fx cache adapter 테스트 |

`RedisKeys` 유틸이 생성한 키 문자열을 그대로 사용. 테스트에서 `RedisKeys.tickLatest(123L)` 같은 호출 결과를 expected로 사용.

---

## BigDecimal 정밀도 주의

- Premium 계산은 BigDecimal 곱셈/나눗셈 — 스케일/반올림 정책에 따라 결과가 다름
- 테스트 expected는 `new BigDecimal("0.012345")` 처럼 문자열 생성자 사용 (double 생성자 금지)
- 비교는 `assertThat(actual).isEqualByComparingTo(expected)` (스케일 무시) 또는 `compareTo == 0`
- `equals()` 비교는 스케일까지 같아야 통과하므로 가급적 피한다

---

## 작업 절차 (이 모듈 한정 보강)

1. `git ls-files modules/market_data/src/main/java` 로 클래스 목록 스캔
2. `application/usecase`, `domain/buffer`, `domain/service`, `infrastructure/cache`, `infrastructure/messaging`, `infrastructure/persistence` 별로 분류해 표 보고
3. **CalPremiumManager 단위 테스트를 가장 먼저 작성** (이 모듈의 위험 표면이 여기에 집중됨)
4. 다음으로 Buffer 계열, Batch Save, Mapper, Cache Adapter, Publisher, Consumer 순
5. 통합 테스트는 마지막. Testcontainers 의존성을 `build.gradle` 에 추가해야 하면 패치 섹션 별도 제출
6. 모든 테스트 작성 후 `./gradlew :market_data:test` 전체 실행, 실패 항목만 빨간 표로 보고

---

## 검증 포인트 (이 모듈 한정)

- [ ] `CalPremiumManager` 의 분기 8종 전부 테스트 케이스 존재
- [ ] BigDecimal 비교는 `isEqualByComparingTo` 사용 (`equals` 사용 0건)
- [ ] `TickBuffer.flush()` 버그 재현 테스트 포함, `@Tag("known-bug")` 부착
- [ ] Kafka 토픽명이 코드에 하드코딩되지 않고 상수 참조 (없으면 별도 보고)
- [ ] Redis 키는 `RedisKeys` 유틸 호출 결과로 검증 (문자열 하드코딩 0건)
- [ ] Testcontainers 사용 시 Docker 미가용 환경 안내 주석 포함
- [ ] 기존 프로덕션 코드 수정 0건 (수정 필요 의심 사항은 `KNOWN_ISSUES.md` 또는 보고만)
