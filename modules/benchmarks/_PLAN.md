# `:benchmarks` 모듈 — JMH 측정 계획

> 갱신: 2026-05-26. `JsonParserBenchmark` 를 두 클래스로 분리하면서 작성.
> 본 모듈은 도메인/애플리케이션 런타임 모듈이 아니라 JSON 파서 교체 의사결정 근거를
> 만드는 측정 전용 모듈이다 (CLAUDE.md §17 참조).

---

## 0. 측정을 두 클래스로 나눈 이유

기존 `JsonParserBenchmark` 한 클래스에 5개 벤치마크가 섞여 있어 다음 비교 비대칭이
결과 해석을 흐리고 있었다.

1. **DSL-JSON 비대칭** — `TickMessage` 측정에서만 `DslTickMessage` 어댑터 record 를 거치며
   변환 비용이 포함됐다. Jackson/Jsoniter 는 변환이 없다.
2. **Jsoniter 비대칭** — `TickMessage` 등 record 타입에 hand-written codec
   (`JsoniterSpi.registerTypeDecoder`) 을 등록해 측정했지만, 운영 코드
   `JsonUtil.fromJson` 은 reflection 경로라 record 처리 실패 가능성이 있다.
3. **batch1000 fixture 단조성** — `Arrays.fill` 로 동일 JSON 인스턴스 1000회 반복이라
   CPU 캐시 효과가 측정값을 부풀린다.
4. **혼합 측정** — 운영 DTO (Upbit/Binance) 측정과 contracts record (`TickMessage`)
   측정이 같은 클래스에 있어, 의미가 다른 두 그룹을 같은 결과 파일에서 비교하게 됐다.

이를 정리해 다음 두 벤치마크 클래스로 분리한다.

---

## 1. `ExchangeDtoBenchmark` — 운영 재현 측정

| 항목 | 내용 |
| --- | --- |
| 대상 | `UpbitOrderbookDto`, `BinanceStreamFormat<BinanceBookTickerDto>` (래퍼 포함) |
| Jackson | `ObjectMapper.readValue` (Binance 는 wrapper 라 `TypeReference`) |
| Jsoniter | `JsonUtil.fromJson(json, Class<T>)` — hand-written codec 금지 |
| DSL-JSON | `DslJsonParserManager.parse(type, json)` — 어댑터 없이 운영 DTO 직접 |
| Serialize 측정 | **없음** — 운영에서 거래소 DTO 를 다시 직렬화하는 hot path 가 없다 |
| Fail-fast | Jsoniter reflection 이 record 처리에 실패하면 `[DIAG]` 로그 + 해당 벤치마크는 `IllegalStateException` |

**측정 의미**: 운영 hot path (거래소 WebSocket raw → DTO) 의 실측 대표값.

---

## 2. `TickMessageBenchmark` — TickMessage 전용, 2개 변형

`TickMessage` 는 `:contracts` 의 Kafka message record. coin_front 까지 SSE 로 흘러가는
Tick 흐름의 직렬화 단위라 운영 hot path 이지만, 현재는 어느 파서도 record 를 직접
처리할 만한 운영 설정이 없다. 따라서 "지금 운영 그대로" 와 "최적 가능" 두 변형을
모두 측정한다.

### 2.1 변형 1 — "AsIs" (현재 운영 경로)

| Parser | 호출 경로 |
| --- | --- |
| Jackson | `ObjectMapper.readValue/writeValueAsString` 그대로 |
| Jsoniter | `JsonUtil.fromJson/toJson` 그대로 — record reflection 경로, 실패하면 결과에 명시 |
| DSL-JSON | `DslTickMessage` 어댑터 왕복 (변환 비용 포함) |

### 2.2 변형 2 — "Optimized" (최적 가능 경로)

| Parser | 호출 경로 |
| --- | --- |
| Jackson | 동일 (개선 여지 없음) |
| Jsoniter | hand-written codec 함수를 **글로벌 SPI 등록 없이** 직접 호출 — AsIs 측정에 영향 X |
| DSL-JSON | `DslJson.tryFindReader/tryFindWriter` 결과를 캐시 후 직접 사용. `TickMessage` 에 `@CompiledJson` 이 적용돼 있을 때만 측정 가능 |

> **글로벌 SPI 회피**: `JsoniterSpi.registerTypeDecoder(...)` 는 정적 상태를 바꾸므로
> AsIs 변형의 `JsonUtil.fromJson` 호출에도 영향을 준다. 두 변형을 같은 JVM 에서
> 깨끗하게 측정하기 위해 Optimized 경로는 `JsonIteratorPool.borrowJsonIterator()` +
> 직접 함수 호출 형태로 구성했다.

### 2.3 Fixture

- 단건: `fixtures/tick-message.json` (기존)
- 변종 16개: `fixtures/tick-message-batch/tick-001.json ~ tick-016.json`
- 분포: BTC 가격대 (~50k) × 4 / ETH (~3k) × 4 / 알트 (1~100) × 4 / 스테이블 (~1.0) × 4
- batch 채우기: `tickMessageBatch[i] = variants[i % variants.length]`

---

## 3. Diagnostics

`@Setup(Level.Trial)` 에서 한 번만 표준출력으로 다음을 기록한다.

```
[DIAG] DSL-JSON TickMessage path: COMPILED | REFLECTION | REFLECTION-FALLBACK or MISSING (variant2 will fail)
[DIAG] Jsoniter TickMessage reflection deserialize: WORKS | FAILS — <className>: <message>
[DIAG] Jsoniter TickMessage reflection serialize: WORKS | FAILS — <className>: <message>
[DIAG] ExchangeDtoBenchmark UpbitOrderbookDto Jsoniter reflection path: WORKS | FAILS — ...
[DIAG] ExchangeDtoBenchmark BinanceStreamFormat Jsoniter reflection path: WORKS | FAILS — ...
```

이 로그가 결과 해석의 핵심 단서다.

---

## 4. 운영 적용 결정 가이드

| 파서 | 운영 전환 비용 | 결정 가이드 |
| --- | --- | --- |
| Jackson | 0 | 이미 사용 중. 기준선. |
| **DSL-JSON** | `:contracts/build.gradle` 에 `annotationProcessor 'com.dslplatform:dsl-json'` + `TickMessage` 에 `@CompiledJson` 추가 (한 줄). 거래소 DTO 는 이미 적용. | `Optimized` 가 AsIs 대비 충분히 빠르면 **그대로 적용 가능**. PR 분리 권장. |
| **Jsoniter** | hand-written codec 작성 + 유지 책임. 새 필드 추가 시 codec 도 수정. | `Optimized` 가 빠르더라도 codec 유지 비용을 감수할 의지가 있을 때만. record 가 늘어나면 부담이 커진다. |

---

## 5. 실행 명령

```powershell
# 전체 (분리 후 두 클래스 + 기존 @Deprecated 클래스 비교)
.\gradlew.bat :benchmarks:jmh

# Exchange DTO 측정만 (~5분)
.\gradlew.bat :benchmarks:jmh -Pjmh.includes=ExchangeDtoBenchmark

# TickMessage 측정만 (~5-10분)
.\gradlew.bat :benchmarks:jmh -Pjmh.includes=TickMessageBenchmark

# sanity (포트폴리오 근거 X)
.\gradlew.bat :benchmarks:jmh -Pjmh.quick=true

# 결과 markdown 갱신
.\gradlew.bat :benchmarks:jmhSummary
```

`jmh.includes` 는 `me.champeau.jmh` 플러그인의 패턴 매칭이라 정규식과 동일하게
`.*Benchmark` 형태로도 지정 가능하다.

---

## 6. 산출물

| 파일 | 역할 |
| --- | --- |
| `modules/benchmarks/build/reports/jmh/results.json` | JMH 원본 결과 |
| `docs/portfolio/json-parser-benchmark.md` | `jmhSummary` 가 만든 포트폴리오 요약 — Exchange DTO / TickMessage AsIs / TickMessage Optimized / AsIs vs Optimized Gap 4개 섹션 |

`jmhSummary` 의 "AsIs vs Optimized Gap" 표는 운영 전환 시 기대 배수를 직접 보여준다.

---

## 7. 다음 단계 (별도 PR)

1. `:contracts/build.gradle` 에 `annotationProcessor 'com.dslplatform:dsl-json:2.0.2'` 추가
2. `TickMessage` 에 `@CompiledJson` 적용
3. 본 모듈 재측정해서 `Optimized` 의 DSL-JSON 값이 실제로 활성화됐는지 확인 (`[DIAG]` 로그가 `COMPILED` 로 바뀜)
4. 결정 후 `JsonParserBenchmark` 삭제 (1주일 후 또는 위 PR 과 함께)
