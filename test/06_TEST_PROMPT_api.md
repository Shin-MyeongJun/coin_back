# 06 — api 테스트 프롬프트

> **선행 첨부 필수**: `00_TEST_COMMON_BASE.md`
> 본 모듈은 정자 `api` 사용 (오타 컨벤션 미적용).

---

## 작업 대상

`modules/api/` 전체.

이 모듈은 **어댑터 레이어**. UseCase 정의는 `query/*` 모듈에 있고, api 모듈은 그 UseCase를 호출하는 컨트롤러 + 합성 서비스 + SSE 만 보유.

핵심 패키지:
- `controller/{meta,economic,analytics,market}/`
- `composition/service/`, `composition/dto/`
- `stream/{handler,controller}/`
- `common/` (`CursorPage`, `OffsetPage`)
- `config/` (`SecurityConfig`, `OpenApiConfig`, `JacksonConfig`, `GlobalExceptionHandler` 등)

---

## 가장 중요한 테스트 대상

### 1. Controller 슬라이스 테스트 (`@WebMvcTest`)

각 컨트롤러를 `@WebMvcTest`로 격리, 의존 UseCase는 `@MockBean`.

| 컨트롤러 | 검증 항목 |
|---|---|
| `meta/ExchangeController` | `GET /api/v1/meta/exchanges` 200, JSON 스키마, 빈 리스트 케이스 |
| `meta/MarketCodeController` | `GET /api/v1/meta/exchanges/{id}/markets`, `GET /api/v1/meta/markets/search?q=&exchange=` |
| `meta/MetaIntegrityController` | `GET /api/v1/meta/integrity` |
| `economic/IndicatorController` | series cursor 페이징, meta 단건, list, change-rate |
| `economic/EconomicCalendarController` | calendar cursor 페이징 |
| `economic/CorrelationController` | correlation 결과 |
| `analytics/CandleController` | `?type=tick\|premium`, mini, downsampled, last-closed |
| `analytics/IndicatorController` | series, latest, latest/multi (`?marketCodeIds=1,2,3`) |
| `analytics/ScreenerController` | `?type=tick\|premium` 단일 조건 스크리너 |
| `market/TickController` | latest 단건, latest bulk |
| `market/PremiumController` | snapshot, series, ranking |
| `market/PremiumDetailController` | raw, agg |
| `market/FxController` | latest, raw, downsampled |
| `composition/...` | 합성 응답 (별도 Controller 또는 Service 직접 테스트) |

**공통 검증**:
- 상태코드 (200, 400, 404)
- `Content-Type: application/json`
- 시간 필드는 epoch ms (long) — `jsonPath("$.items[0].ts").value(any(Long.class))`
- 페이징 응답은 `CursorPage` / `OffsetPage` envelope:
  - cursor: `{ items, nextCursor, hasMore }`
  - offset: `{ items, page, size, total }`
- 단건/비페이징 리스트는 raw (envelope 없이 그대로)
- UseCase 호출 인자 (`ArgumentCaptor`로 path/query 파라미터가 정확히 전달되는지)

### 2. RFC 7807 ProblemDetail (전역 예외 핸들러)

`config/GlobalExceptionHandler.java` (`@RestControllerAdvice`).

검증 시나리오:
- 존재하지 않는 경로 → 404 + `application/problem+json` + ProblemDetail 스키마
- `IllegalArgumentException` → 400
- `NoSuchElementException` → 404
- 검증 실패 (`@Valid` 위반) → 400 + 필드별 errors
- 예상치 못한 예외 → 500 (디테일 마스킹 정책 확인)

ProblemDetail 필수 필드: `type`, `title`, `status`, `detail`, `instance`. 추가 필드(`timestamp`, `traceId` 등)가 있으면 같이 검증.

### 3. CursorPage / OffsetPage (record)

`common/CursorPage.java`, `common/OffsetPage.java`.

- record 생성 + Jackson 직렬화 결과 (`@JsonTest` 슬라이스)
- 빈 items, 마지막 페이지 (hasMore=false / nextCursor=null), 첫 페이지
- 시간 필드 직렬화 정책 (epoch ms long)

### 4. Composition Service (단위 테스트)

`composition/service/` 의 합성 서비스. 다수 query UseCase mock 후:
- 호출 순서/병렬성
- 시간 동기화 (캔들 + 지표 interval 매칭)
- 누락 데이터 처리 (한쪽 빈 결과)
- 결과 record 정확성

### 5. SSE Stream (`stream/`)

#### 5-1. Sink fanout 허브 (`MarketDataStream`, `AnalyticsStream`)

`Sinks.Many<DOMAIN>` 시그니처 검증 (DTO/JSON이 아닌 도메인 객체).

```java
@Test
void publishesToAllSubscribers() {
    Sinks.Many<Tick> sink = Sinks.many().multicast().onBackpressureBuffer();
    StepVerifier.create(sink.asFlux().take(3))
            .then(() -> sink.tryEmitNext(tickFixture(1)))
            .then(() -> sink.tryEmitNext(tickFixture(2)))
            .then(() -> sink.tryEmitNext(tickFixture(3)))
            .expectNextCount(3)
            .verifyComplete();
}
```

#### 5-2. SSE Handler (`TickSseHandler`, `PremiumSseHandler`, `CandleCloseSseHandler`, `IndicatorCloseSseHandler`)

- marketCodeId 필터링 정확성 (구독자가 요청한 marketCodeId만 수신)
- 직렬화 (도메인 → JSON)
- emitter 타임아웃 30분 (실제 30분 기다리지 말고, 짧은 타임아웃으로 주입 가능한지 확인)
- `onCompletion` / `onTimeout` / `onError` 시 Disposable 해제 (`StepVerifier.dispose()` 검증)

#### 5-3. Kafka → Sink 브릿지

`stream/...` 안에 Kafka consumer가 있고 메시지를 Sink로 emit. 통합 테스트:
- Testcontainers Kafka에 메시지 publish
- Sink 구독자가 메시지 수신 (Awaitility로 비동기 대기)
- consumer group-id가 UUID 기반이라 다중 인스턴스 fanout 보장 (구현 확인)

#### 5-4. `StreamController`

- SSE 엔드포인트 (`text/event-stream`) 응답
- MockMvc로는 SSE 검증이 제한적이므로, `WebTestClient` 또는 직접 HTTP 클라이언트 사용 고려
- 단순 케이스: 컨트롤러가 SseEmitter를 반환하고 헤더가 정확한지 확인

### 6. Config 빈 로드 검증 (`@SpringBootTest`)

- `SecurityConfig`: `permitAll()` 상태에서 모든 엔드포인트 접근 가능 + CSRF disable + CORS dev 허용
- `OpenApiConfig`: dev 프로파일에서 `/swagger-ui.html` 접근 가능, prod 프로파일에서는 차단
- `JacksonConfig`: epoch ms 직렬화, BigDecimal 문자열 직렬화
- `HeartbeatConfig`: `ModuleName.API` 등록

프로파일별 분리:
```java
@SpringBootTest
@ActiveProfiles("dev")
class SwaggerDevIT { ... }

@SpringBootTest
@ActiveProfiles("prod")
class SwaggerProdIT { ... }
```

### 7. 의존성 경계 검증 (ArchUnit 권장)

이 모듈은 `query/* + contracts + infra_shard + infra_heartbeat` 만 의존 가능. 도메인 모듈(market_data, analytics 등) 직접 의존 금지.

ArchUnit 룰:
```java
@Test
void api_does_not_depend_on_domain_modules() {
    JavaClasses classes = new ClassFileImporter()
            .importPackages("com.example.demo.api");
    noClasses().that().resideInAPackage("com.example.demo.api..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.example.demo.market_data..",
                    "com.example.demo.analystics..",
                    "com.example.demo.meta_data..",
                    "com.example.demo.ingestion.."
            )
            .check(classes);
}
```

이 한 가지 룰만 있어도 향후 회귀를 막는다.

### 8. 합성 엔드포인트 (`composition/`)

- query 모듈 UseCase만 호출, 도메인 모듈 직접 호출 0건 (ArchUnit으로 보강)
- 합성 응답 record는 `composition/dto/` 안에만 위치 (ArchUnit)
- interval/시간 동기화 로직 단위 테스트 — 단위 테스트 보유 필수 (api/_PLAN.md §6 검증 포인트)

---

## fixture / 도우미

`query/*` 모듈의 View record를 그대로 사용하는 게 원칙. fixture는 view record 빌더만:

```java
public final class ApiFixtures {
    public static MarketCodeView marketCodeView(Long id, String code) {
        return new MarketCodeView(id, code, ...);
    }
}
```

---

## 토픽 / Redis 키

api 모듈은 토픽 신설 없음 (consume only). Redis 캐시 사용 시 `RedisKeys` 유틸.

---

## 작업 절차 (이 모듈 한정)

1. `git ls-files modules/api/src/main/java` 스캔
2. 분류:
   - `controller/{meta,economic,analytics,market}/`
   - `composition/`
   - `stream/`
   - `common/`
   - `config/`
3. **순서**:
   1. `common/` (CursorPage, OffsetPage) — `@JsonTest`
   2. `controller/meta/` — `@WebMvcTest` (가장 단순)
   3. `controller/economic/` — cursor 페이징 패턴 정착
   4. `controller/analytics/` + `controller/market/` — 도메인 풍부
   5. `config/GlobalExceptionHandler` — ProblemDetail
   6. `composition/` — 합성 단위 테스트
   7. `stream/` — Sink + StepVerifier (단위) → Kafka 통합
   8. ArchUnit — 의존성 경계
   9. Config 통합 테스트 — `@SpringBootTest` + 프로파일 분리
4. ArchUnit 의존성을 `build.gradle` 에 추가해야 하면 별도 패치 섹션
5. WireMock / Testcontainers 의존성도 마찬가지

---

## 검증 포인트 (이 모듈 한정)

- [ ] 모든 컨트롤러 `@WebMvcTest` 슬라이스, UseCase는 `@MockBean`
- [ ] 시계열 = cursor, 메타/리스트 = offset 페이징 일관 적용
- [ ] 시간 필드 epoch ms (long), ISO 문자열 0건
- [ ] ProblemDetail (RFC 7807) 응답 스키마 검증 (`type`/`title`/`status`/`detail`/`instance`)
- [ ] `CursorPage` / `OffsetPage` 직렬화 (`@JsonTest`)
- [ ] composition 서비스 — query UseCase만 호출, 도메인 모듈 호출 0건 (ArchUnit으로 강제)
- [ ] interval/시간 동기화 로직 단위 테스트 (api/_PLAN.md §6 요구)
- [ ] SSE Sink 시그니처가 도메인 객체 (`Sinks.Many<Tick>`) — DTO/JSON 아님 (StepVerifier로 검증)
- [ ] SSE 핸들러 안에 Kafka 구독 코드 0건 (Sink만 구독) — ArchUnit 또는 코드 리뷰
- [ ] consumer group-id UUID 기반 fanout 정책 검증
- [ ] emitter 타임아웃 / Disposable 해제 (`onCompletion`/`onTimeout`/`onError`)
- [ ] Swagger dev 프로파일 노출 / prod 차단 (프로파일 분리 통합 테스트)
- [ ] ArchUnit 의존성 경계 룰 1건 이상
- [ ] `application-test.yml` 시크릿 placeholder
