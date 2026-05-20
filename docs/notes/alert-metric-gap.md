# AlertMetric ↔ analytics indicator 갭 평가

> 작성일: 2026-05-19
> 범위: 이 노트는 **결정 권고**만 다룹니다. 코드 변경은 본 번들에서 수행하지 않습니다.

## 1. 현재 상태 (코드 기준)

### 1.1 `:alert` `AlertMetric` enum

파일: `modules/alert/src/main/java/com/example/demo/alert/domain/domain/AlertMetric.java`

```java
public enum AlertMetric {
    BUY_PREMIUM_RATE,
    SELL_PREMIUM_RATE
}
```

실제 enum 값은 위 두 개뿐입니다. 현재 alert 평가는 `RuleMatcher` + `EvaluateMarketSignalUseCase` 경로를 사용하며, market signal로 변환 가능한 metric도 위 두 개에 한정됩니다.

이전 작업 메모/계획 문서(예: 이 번들 task description, coin_front 가정안)에서 언급된 `LAST_PRICE`, `RSI`, `MACD`, `BOLLINGER_UPPER`, `BOLLINGER_LOWER` 는 **아직 enum 에 추가되지 않았습니다.**

### 1.2 `:analytics` indicator 실제 지원

파일: `modules/analytics/src/main/java/com/example/demo/analystics/domain/domain/indicator/TradeIndicatorType.java` 는 다음 enum 값을 가집니다.

```text
MEAN, STDDEV, MAX, MIN,
PERCENTILE_25, PERCENTILE_50, PERCENTILE_75,
RSI, EMA, MACD, TR, ATR, SMA,
BOLLINGER_BANDS, STOCHASTIC
```

하지만 실제로 state updater / state codec 까지 구현되어 흐름을 타는 지표는 다음으로 한정됩니다 (2026-05-19 점검 기준):

| 지표 | Updater | State codec | 메모 |
| --- | --- | --- | --- |
| `EMA` | `EmaUpdater` | `EmaStateCodec` | OK |
| `RSI` | `RsiUpdater` | `RsiStateCodec` | OK |
| `STDDEV` | (streaming) | `StddevStateCode` (오타 유지) | OK |
| `TR` | (streaming) | `TrStateCodec` | OK |
| `MEAN` | (streaming) | `MeanStateCodec` | OK |

즉, `MACD`, `BOLLINGER_BANDS`, `STOCHASTIC`, `ATR`, `SMA`, percentile 계열, `MAX/MIN` 은 enum 에는 있지만 **state 계산이 붙어 있지 않아 alert metric 으로 직접 사용할 수 없습니다.**

### 1.3 coin_front 가정과의 차이

coin_front 알람 화면은 다음 metric 을 가정합니다 (placeholder):

- `LAST_PRICE`
- `RSI`
- `MACD`
- `BOLLINGER_UPPER`, `BOLLINGER_LOWER`

이 중 `RSI` 만 analytics 가 실제로 흐름을 흘리고 있으며, 나머지는 백엔드 미지원입니다.

## 2. 갭 요약

| Metric | analytics 실제 지원? | 1차 추천 |
| --- | --- | --- |
| `BUY_PREMIUM_RATE` | 백엔드 자체 계산 (analytics 무관) | 유지 (이미 동작) |
| `SELL_PREMIUM_RATE` | 동일 | 유지 (이미 동작) |
| `LAST_PRICE` | `market-data.tick` 그대로 사용 가능 (analytics 거치지 않음) | 가능 — bridge 추가만 필요 |
| `RSI` | OK (`RsiUpdater` + state codec) | 가능 — analytics indicator topic bridge 필요 |
| `MACD` | enum 만 존재, state 미구현 | **불가 (분석 도메인 추가 작업 선행)** |
| `BOLLINGER_UPPER` / `_LOWER` | 동일 | **불가** |

## 3. 결정 후보

### A) `AlertMetric` 에서 MACD/Bollinger 제거

장점:
- 코드와 enum 이 즉시 일치.
- 프론트는 placeholder 만 사용 중이라 노출되지 않음.

단점:
- coin_front 가 가정해 둔 placeholder UI 옵션을 제거해야 함 (조정 비용 있음).
- 분석 도메인 확장 시 다시 enum 을 늘려야 함.

### B) `:analytics` 에 MACD / Bollinger state codec 추가

장점:
- 프론트 placeholder 와 백엔드 enum 이 모두 충족됨.
- analytics 확장이 자연스럽게 진행됨.

단점:
- 별도 PR 필요. MACD 는 EMA(12)/EMA(26)/Signal(EMA9) 세 가지 상태를 추적해야 하고, Bollinger 는 SMA(20) + STDDEV(20) 결합이라 state codec 두 개 이상 추가 + 윈도우 정책 결정 필요.
- partition lifecycle / Redis recovery 까지 함께 검증 필요.

### C) MVP 는 `PREMIUM_RATE` / `LAST_PRICE` / `RSI` 만 활성화, 나머지는 enum disabled flag

장점:
- 프론트 placeholder 를 유지하면서, 백엔드에서 disabled metric 은 일관되게 reject 가능.
- analytics 도메인 확장 일정과 분리된 step-by-step 출시 가능.

단점:
- enum 에 `enabled` 같은 field 를 추가해야 하므로 enum constant 자체에 부가 정보가 붙음 — 도메인 record/enum 컨벤션과 충돌 여지 있음.
- 프론트 mock → real 전환 시 추가 동작 (예: 백엔드 reject 결과 처리) 분기를 만들어야 함.

## 4. 권고

**C) 우선 + 단기 후행 작업으로 A 또는 B 결정.**

이유:
1. 현 시점 코드와 가장 충돌이 작음 (enum 항목은 그대로 두고 활성 여부만 도메인 차원에서 표시).
2. 프론트 placeholder 가 사라지지 않음 → 디자인 회귀 최소.
3. analytics 도메인 확장(`B`) 은 별도 backlog 로 분리하기에 충분히 크므로 본 번들의 outbox/migration 정리 작업과 묶지 않는 편이 안전.
4. 단기적으로 `LAST_PRICE` 는 analytics 없이 `market-data.tick` 만으로 추가 가능하므로, 같은 PR 에서 `AlertMetric.LAST_PRICE` 만 enable 처리하는 식의 점진적 확장이 가능.

## 5. 본 번들 처리 범위

이 노트는 **권고 문서**입니다. 코드 변경 (enum 수정, evaluator 분기 추가, analytics state codec 추가 등) 은 별도 PR 로 진행합니다.
