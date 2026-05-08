# 02 — analytics 테스트 프롬프트

> **선행 첨부 필수**: `00_TEST_COMMON_BASE.md`
> 패키지 오타 `analystics` 그대로 유지.

---

## 작업 대상

`modules/analytics/` 전체.

핵심 도메인:
- 캔들: `TickCandle`, `PremiumCandle`, `PremiumDetailCandle`
- 캔들 종가 (sealed interface): `CloseCandle` permits `TickCloseCandle`, `PremiumCloseCandle`, ...
- 지표: `TickIndicator`, `PremiumIndicator`
- 지표 종가 (sealed interface): `CloseIndicator` permits `TickCloseIndicator`, `PremiumCloseIndicator`
- 지표 상태 (sealed interface): `IndicatorState`
- 복원 상태 (sealed interface): `RecoveryState` permits `RecoveryCandleState`, `RecoveryIndicatorState`
- Interval: `1m`, `5m`, `15m`, `1h`, ...
- 키: `TickKey`, `PremiumKey`

---

## 이 모듈의 테스트가 까다로운 이유 (테스트 작성 전 인지)

1. **상태(state) 보유 모듈**: Store/Buffer/PartitionRegistry 가 인메모리 상태를 누적
2. **파티션 단위 격리**: Kafka partition revoke/assign에 따라 상태가 이동. Redis로 저장/복원
3. **시간 의존**: Interval 경계에서 캔들이 닫힘 → 결정적 테스트를 위해 `Clock` 주입 또는 시간 파라미터를 명시적으로 받는 테스트 헬퍼 필요
4. **BigDecimal 누적 계산**: EMA/RSI/STDDEV/TR/MEAN — 부동소수점 기대값을 손으로 계산해야 함

---

## 가장 중요한 테스트 대상

### 1. `CandleStore` 계열 (`TickCandleStore`, `PremiumCandleStore`, `PremiumDetailCandleStore`)

- `update(key, val)`: 같은 Interval 버킷 내에서는 OHLC가 누적
  - O: 첫 값 고정 (이후 변경 없음)
  - H: max(누적, 신규)
  - L: min(누적, 신규)
  - C: 마지막 값
- 새 버킷 진입 시 직전 버킷이 close 처리되는지
- `getCandles(interval)` 가 현재 진행 중인 캔들 + 닫힌 캔들 리스트를 반환하는지
- `assign(snapshot)` 으로 Map 통째 복원 시 기존 상태 덮어쓰기
- `drain(interval)` 호출 시 `CloseCandle` sealed 타입의 정확한 구현체 반환 + 내부 상태 클리어
- 동시성: `update` + `drain` 동시 호출 시 데이터 손실/중복 없는지 (CountDownLatch + 다수 스레드)

### 2. `IndicatorStore` 계열

- EMA: `EMA_t = α * P_t + (1-α) * EMA_{t-1}`, α = 2/(N+1)
  - 초기값 정책 (SMA로 시드 vs P_1으로 시드) 확인 후 검증
- RSI: 14 기간 표준 — 손계산 expected (또는 검증된 외부 라이브러리 결과와 비교)
- STDDEV / MEAN / TR — 각각 수식 따라 손계산
- BigDecimal 정밀도: `setScale()` 정책 확인 후 expected 생성
- `assign(snapshot)` 복원 후 다음 update가 끊김 없이 이어지는지 (가장 어려운 케이스)

### 3. `PartitionRegistry` (`TickPartitionRegistry`, `PremiumPartitionRegistry`)

- `assignPartition(id)`: candleStore + indicatorStore 동시 생성
- `revokePartition(id)`: 두 store 동시 제거
- `update(partitionId, key, val)`: 해당 partition의 두 store에 모두 전달되는지
- 미존재 partition으로 `update` 호출 시 경고 로그 + skip (예외 던지지 않음)
- `restoreCandles` / `restoreIndicators`: 미존재 partition도 자동 assign 후 복원
- `flushCandles(interval)` / `flushIndicators(interval)`: 모든 partition을 가로질러 drain한 결과를 평탄화
- `getActivePartitionIds()` 정확성

### 4. Sealed Interface 분기 (`CloseCandle`, `CloseIndicator`, `IndicatorState`, `RecoveryState`)

- exhaustive switch 패턴 검증: 새 permits 추가 시 컴파일 에러가 나는지 (의도된 sealed 사용)
- 매퍼/Publisher가 각 변형(variant)을 모두 처리하는지 — 변형별 케이스 작성

### 5. Redis 상태 코덱 (`PremiumDetailStateCodec` 등)

- encode → decode 라운드트립 (모든 필드 동치, 특히 BigDecimal 정밀도)
- 잘못된 포맷 입력 시 명확한 예외
- SEP 토큰 이스케이프 정책 (값에 SEP가 들어가는 경우 동작)
- 빈 문자열 / null 입력 처리

### 6. 리밸런싱 시나리오 (통합 테스트 1~2개)

- partition revoke 시: PartitionRegistry → 상태 추출 → Redis 저장 (`SaveStatePort`) 동작
- partition assign 시: Redis 조회 → PartitionRegistry 복원 동작
- 같은 partitionId가 revoke 후 다시 assign되면 이전 상태가 복원되는지

### 7. Scheduler (`@Scheduled` flush)

- 단위 테스트: 스케줄러 메서드 직접 호출 시 PartitionRegistry.flush + DB save + Kafka publish 순서 (`InOrder`)
- `@Scheduled` 자체는 검증하지 않음 (cron 표현식 검증은 별도 `CronExpression.parse()` 정도만)

### 8. Kafka Consumer (TickAnalyticsService 등)

- 메시지 수신 → 매퍼 → PartitionRegistry.update 호출
- partition 정보가 ConsumerRecord에서 정확히 추출되는지

### 9. Kafka Publisher

- 토픽명: `analytics.tick-candle`, `analytics.premium-candle`, `analytics.tick-indicator`, `analytics.premium-indicator` 등
- 메시지 key 정책 (marketCodeId? interval? 합성키?) 확인 후 검증

---

## 토픽 / Redis 키 검증

| 종류 | 패턴 | 검증 위치 |
|---|---|---|
| Kafka (consume) | `market-data.tick`, `market-data.premium`, `market-data.premium-detail` | Consumer 테스트 |
| Kafka (publish) | `analytics.{tick\|premium}-{candle\|indicator}` | Publisher 테스트 |
| Redis 상태 | `ys:{env}:v1:{tick\|premium}:candle:state:{partitionId}:{interval}` | Codec/SaveState 테스트 |
| Redis 상태 | `ys:{env}:v1:{tick\|premium}:indicator:state:{partitionId}:{interval}` | Codec/SaveState 테스트 |

전부 `RedisKeys` 유틸 호출 결과로 검증.

---

## BigDecimal 누적 계산 — 테스트 작성 전략

- expected 값을 손으로 계산하기 어려우면, **검증된 참조 구현 (TA-Lib, ta4j 등)** 의 결과를 fixture 에 박아둔다 (외부 의존 추가 금지, 결과만 fixture).
- fixture 위치: `src/test/resources/fixtures/indicators/<name>.json`
- 한 fixture 당 input series + 기대 EMA/RSI/STDDEV 값 동봉
- BigDecimal 비교는 `isEqualByComparingTo` (스케일 무관)

---

## 동시성 테스트 패턴

```java
// given
int threads = 8;
int perThread = 1000;
ExecutorService es = Executors.newFixedThreadPool(threads);
CountDownLatch start = new CountDownLatch(1);
CountDownLatch done = new CountDownLatch(threads);

// when
for (int i = 0; i < threads; i++) {
    es.submit(() -> {
        start.await();
        for (int j = 0; j < perThread; j++) {
            store.update(keyOf(j), valueOf(j));
        }
        done.countDown();
        return null;
    });
}
start.countDown();
assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();

// then
List<? extends CloseCandle> drained = store.drain(Interval.M1);
assertThat(drained).hasSizeLessThanOrEqualTo(threads * perThread);
// 손실/중복이 없는지 합산 검증
```

---

## 작업 절차 (이 모듈 한정)

1. `git ls-files modules/analytics/src/main/java` 스캔
2. 분류: `domain/store`, `domain/buffer`, `domain/partition_registry`, `domain/domain` (sealed), `infrastructure/cache/.../codec`, `infrastructure/messaging`, `infrastructure/scheduler`
3. **순서**: Store → IndicatorStore → PartitionRegistry → Codec → Scheduler → Consumer → Publisher → 리밸런싱 통합
4. fixture JSON 도입 시 별도 보고 (위치 + 출처)
5. 동시성 테스트는 `@Tag("concurrency")` 부착 (CI에서 격리 가능하도록)

---

## 검증 포인트 (이 모듈 한정)

- [ ] CandleStore: 동일 버킷 OHLC 누적 + 버킷 경계 close 시나리오 모두 커버
- [ ] IndicatorStore: EMA/RSI/STDDEV/TR/MEAN 각 1개 이상 fixture 기반 케이스
- [ ] PartitionRegistry: assign/revoke/restore/flush 4종 케이스 + multi-partition 시나리오
- [ ] sealed interface 변경 시 permits 누락이 컴파일 타임에 잡히는지 확인 (테스트 코드가 모든 variant를 다루는지)
- [ ] Redis 상태 codec 라운드트립 (BigDecimal 정밀도 무손실)
- [ ] 리밸런싱 통합 테스트: revoke → save → assign → restore 흐름
- [ ] 동시성 테스트 `@Tag("concurrency")` 부착
- [ ] BigDecimal 비교 `isEqualByComparingTo` 사용 (`equals` 0건)
- [ ] 패키지 `analystics` 오타 유지
