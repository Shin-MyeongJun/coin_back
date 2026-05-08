# 04 — ingestion 테스트 프롬프트

> **선행 첨부 필수**: `00_TEST_COMMON_BASE.md`
> 디렉토리 오타 `ingection` 그대로 유지. 클래스 패키지는 `ingestion` 정상 표기.
> `infre_exchange` (디렉토리/패키지 모두 오타) 그대로.

---

## 작업 대상

`modules/ingestion/exchange/*` + `modules/ingestion/fx_ingestion/`

이 단계에서는 **economic 하위는 제외** (별도 프롬프트 05에서 처리).

핵심 모듈:
- `ingestion/exchange/ingestion_exchange_shard` ← 공통 (`ExchangeStreamManager` 등 베이스)
- `ingestion/exchange/binance_ingestion`
- `ingestion/exchange/upbit_ingestion`
- `ingestion/fx_ingestion` ← 네이버 환율 크롤링

이 모듈의 핵심 책임: **외부 데이터 소스 → 도메인 변환 → Kafka publish (only)**.
DB persistence는 없다 (저장은 market_data가 담당).

---

## 이 모듈 테스트가 까다로운 이유

1. **외부 의존성이 큼**: WebSocket (binance/upbit), HTTP (네이버 환율, upbit REST)
2. **`infra_exchange`에 클라이언트 위치**: ingestion은 그 클라이언트를 호출만 하므로, **mocking 경계는 `infra_exchange`의 클라이언트 인터페이스**
3. **JSON 파싱 라이브러리 다양**: DSL-JSON (`@CompiledJson`), Jsoniter, Jackson 혼용 — 라이브러리별 특성 검증 필요
4. **재연결/백오프**: WebSocket 끊김 시 재연결 로직이 핵심 — 결정적 테스트가 어려움 → Awaitility 필수

---

## 가장 중요한 테스트 대상

### 1. `RawToMessage<RAW, MESSAGE>` 매퍼 (최우선)

이 모듈의 정확성을 결정. JSON 한 줄 잘못 파싱하면 다운스트림이 전부 꼬인다.

테스트 대상:
- `infre_exchange/upbit/dto/UpbitTickerDto` → `TickRawMessage` 매퍼
- `infre_exchange/binance/dto/BinanceTradeDto` → `TickRawMessage` 매퍼
- 기타 모든 RawToMessage 구현

검증 시나리오:
- **정상 케이스**: 실제 거래소 응답 JSON fixture (`src/test/resources/fixtures/upbit/ticker.json` 등)을 그대로 파싱 → 모든 필드 매핑 정확성
- **필드 누락**: optional 필드 누락 시 null 또는 기본값
- **타입 변환**: `BigDecimal` 정밀도 (특히 `tp`, `op`, `hp`, `lp` 등 가격 필드)
- **timestamp**: `tms` (ms) → 도메인의 timestamp 필드로 그대로 전달
- **enum 매핑**: `c` ("RISE"/"EVEN"/"FALL"), `ms` ("ACTIVE" 등), `mw` ("CAUTION") 매핑
- **Boolean 필드**: `its` (Deprecated), `warning` 등
- **잘못된 JSON**: 명확한 예외 (DSL-JSON / Jsoniter 별 예외 타입 확인)

### 2. WebSocket Handler / Listener (`*RawHandler`)

Mocking 경계 결정:
- `infra_exchange/{binance,upbit}` 의 WebSocket 클라이언트가 메시지를 콜백으로 전달한다고 가정
- ingestion 측 핸들러는 그 콜백을 받아 RawToMessage → Publisher로 흘림

테스트:
- 콜백 호출 시 매퍼 호출 + Publisher 호출 (mock으로 검증)
- 메시지 파싱 실패 시 에러 핸들링 (로그 + skip 또는 DLT)
- 메시지 폭주 시 backpressure 동작 (큐 사이즈, drop 정책)

### 3. `ExchangeStreamManager` (ingestion_exchange_shard 공통)

핵심 기능:
- 거래소별 마켓 코드 리스트 로딩 → 구독
- 연결 끊김 감지 → 재연결 (백오프)
- 마켓 코드 변경 시 구독 갱신

테스트:
- `start()` 호출 시 전체 마켓 코드에 대해 구독 시도
- `disconnect` 이벤트 → 재연결 트리거 (`Awaitility.await().atMost(...).until(...)`)
- 재연결 백오프: 1s → 2s → 4s ... 상한선 (`@Tag("slow")` 부착, 시간 모킹 가능하면 `Clock` 주입)
- `stop()` 호출 시 모든 자원 정리 (WebSocket close, scheduler shutdown)

### 4. Kafka Publisher (`*Publisher`)

토픽 검증 (이 모듈은 publish only이므로 가장 중요):

| 모듈 | 토픽 |
|---|---|
| `binance_ingestion` | `ingestion-exchange.tick-raw` |
| `upbit_ingestion` | `ingestion-exchange.tick-raw` |
| `fx_ingestion` | `ingestion-fx.fx-raw` (또는 프로젝트 컨벤션 확인) |

- Testcontainers Kafka에 publish → 별도 KafkaConsumer로 수신
- 메시지 key가 `marketCodeId` 또는 거래소 마켓 식별자인지
- payload 가 `TickRawMessage` JSON 직렬화 결과와 동치
- 파티셔닝: 같은 marketCodeId가 같은 partition에 가는지 (downstream의 partition 일관성 보장 위해)

### 5. `fx_ingestion` (네이버 환율 크롤링)

특수성: HTTP 크롤링 + HTML 파싱 가능성. 또는 JSON API.

- **WireMock 사용 권장**: 네이버 응답 fixture를 박아두고 HTTP 호출을 mock
- 응답 변경 시 파싱 실패 → 명확한 예외 + 로그
- 스케줄링 주기 (몇 초/분마다 호출하는지) — 단위 테스트는 메서드 직접 호출
- 환율 추출 정확성 (BigDecimal 정밀도, 소수점 자릿수)

WireMock 설정 예:
```java
@RegisterExtension
static WireMockExtension wm = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

@DynamicPropertySource
static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("ingestion.fx.naver.base-url", () -> wm.baseUrl());
}
```

### 6. `infra_exchange` 클라이언트 (의존성 검증만)

ingestion 모듈 테스트 범위에서는 `infra_exchange` 자체를 깊이 테스트하지 않는다.
다만:
- ingestion 모듈이 `infra_exchange` 의 인터페이스를 정확히 사용하는지 (계약 테스트)
- `UpbitAuthTokenProvider` 같은 유틸은 단위 테스트 (JWT 생성/SHA512 해시 정확성)

---

## 토픽 / Redis 키

이 모듈은 Redis 캐시를 거의 쓰지 않음 (publish only). Redis 사용처가 있다면 `RedisKeys` 유틸 검증.

| 종류 | 패턴 |
|---|---|
| Kafka publish | `ingestion-exchange.tick-raw` |
| Kafka publish | `ingestion-fx.fx-raw` |

---

## fixture 관리

거래소 응답 JSON은 시간이 지나면 포맷이 바뀐다. fixture에 **수집 일자 + 출처 URL을 주석 또는 별도 README**로 명기.

```
src/test/resources/fixtures/
├── upbit/
│   ├── ticker_2026-05-01.json     # 출처: wss://api.upbit.com/websocket/v1
│   └── README.md                  # 수집 방법 설명
└── binance/
    └── trade_2026-05-01.json
```

---

## 작업 절차 (이 모듈 한정)

1. `git ls-files modules/ingestion/exchange modules/ingestion/fx_ingestion` 스캔
2. 분류:
   - 매퍼 (RawToMessage)
   - WebSocket Handler / HTTP Crawler
   - StreamManager (ingestion_exchange_shard)
   - Publisher
   - infra_exchange 유틸 (UpbitAuthTokenProvider 등)
3. **순서**: 매퍼 → 유틸 → Handler → Publisher → StreamManager → fx_ingestion (WireMock)
4. WebSocket 연결 자체는 mocking. 실제 WebSocket 통신은 검증 범위 밖
5. WireMock 의존성을 `build.gradle`에 추가해야 하면 별도 패치 섹션

---

## 검증 포인트 (이 모듈 한정)

- [ ] 거래소별 RawToMessage 매퍼 — 실제 응답 fixture 기반 파싱 정확성
- [ ] Upbit/Binance JSON 라이브러리 (DSL-JSON / Jsoniter / Jackson) 별 특성 차이 인지
- [ ] BigDecimal 정밀도 무손실 (가격 필드 모두 문자열 → `BigDecimal` 검증)
- [ ] Kafka 토픽명 `ingestion-exchange.tick-raw`, `ingestion-fx.fx-raw` 정확히 사용
- [ ] 같은 marketCodeId의 메시지가 같은 partition에 가는 파티셔닝 정책 검증
- [ ] WebSocket 재연결 백오프 (`@Tag("slow")`)
- [ ] WireMock 으로 네이버 환율 mocking
- [ ] `infra_exchange` 클라이언트 자체는 깊이 테스트 안 함 (별도 모듈)
- [ ] `infre_exchange` 오타 패키지 유지
- [ ] fixture 파일에 수집 일자 / 출처 명기
