# 05 — economic 테스트 프롬프트

> **선행 첨부 필수**: `00_TEST_COMMON_BASE.md`
> 디렉토리 오타 `ingection` 유지, 클래스 패키지는 `com.example.demo.ingestion.economic.economic_ind` 정상 표기.

---

## 작업 대상

`modules/economic/`

- `economic_ind/economic_ind_shard` ← 베이스 클래스 / Port / 매퍼 / `EcoScheduleCache` / `EcoDynamicIndScheduler`
- `economic_ind/fred` ← FRED API 기반
- `economic_ind/crawling` ← Investing.com / Yahoo Finance 크롤링

---

## 이 모듈의 특수성 (테스트 작성 전 인지)

1. **수집 + 저장 + Kafka relay 통합 책임**: ingestion과 달리 한 모듈에서 모두 수행 (시계열 데이터 자체가 최종 도메인 값이라 후처리 파이프라인 불필요 — 02_PROJECT_CONTEXT.md §7-6)
2. **`SyncScheduleService<RAW>`가 베이스**: FRED와 Crawling 양쪽이 이 추상 클래스를 상속. 베이스 자체를 테스트하려면 fake RAW 타입을 만들어 검증
3. **캐시 갱신은 어댑터 책임**: UseCase가 아닌 `EcoScheduleSaveAdapter`, `EcoCodeSaveAdapter`가 DB 저장 후 캐시 갱신 (책임 경계 중요)
4. **외부 API 의존**: FRED REST, Investing/Yahoo HTML 또는 JSON
5. **토픽 prefix 유지**: 분리 이후에도 기존 토픽명 유지 (consumer 영향 차단) — 토픽명을 임의로 바꾸지 말 것

---

## 가장 중요한 테스트 대상

### 1. `SyncScheduleService<RAW>` (베이스 단위 테스트, 최우선)

`modules/economic/economic_ind/economic_ind_shard/.../SyncScheduleService.java` 의 `sync()` 메서드 흐름 검증.

흐름:
1. `getters.getRaws()` → 외부 데이터 조회
2. `rawToDomain.toDomain(raw, null)` → 도메인 변환
3. `writeScheduleSPort.saveAll(fetchedSchedules)` → Schedule DB 저장
4. `EconomicSchedule::getCode` 추출 → `writeEcoIndCodePort.saveAll(codes)` → Code DB 저장
5. `readScheduledPort.readPending()` → 최신 스케줄 재조회
6. `readEcoIndCodePort.readAll()` → 최신 코드 재조회
7. (TODO: 캐시 일괄 동기화)

테스트 작성 전략:
- **fake RAW 타입** (예: `record FakeRaw(String id, String value)`) 으로 베이스 추상 클래스를 인스턴스화
- 6개 Port 전부 mock
- `InOrder` 로 호출 순서 검증 (1→3→4→5→6)
- step 4: 추출된 codes 가 fetchedSchedules 와 매칭되는지 (`ArgumentCaptor` + `usingRecursiveComparison`)
- step 5/6의 결과가 다음 단계(현재 TODO)에 사용되는지 — 현재는 사용 안 하므로 호출만 검증

### 2. FRED 어댑터 (`fred` 하위)

- FRED API REST 응답 → `RAW` 타입 매퍼
- HTTP mocking은 **WireMock 권장**
- API key는 `application-test.yml`에 placeholder, `@DynamicPropertySource`로 주입
- 응답 변경 / 에러 응답 (4xx, 5xx) 처리
- rate limit 정책 (초당 호출 수) 있으면 검증

WireMock 설정:
```java
@RegisterExtension
static WireMockExtension fred = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

@DynamicPropertySource
static void register(DynamicPropertyRegistry registry) {
    registry.add("economic.fred.base-url", fred::baseUrl);
    registry.add("economic.fred.api-key", () -> "test-key");
}
```

### 3. Crawling 어댑터 (`crawling` 하위)

- Investing.com / Yahoo Finance HTML 또는 JSON 응답 → 도메인 변환
- HTML 파싱 라이브러리 사용 시 (Jsoup 등) selector 안정성 검증
- WireMock으로 HTML fixture 응답
- 페이지 구조 변경에 강인한 설계인지 (selector가 너무 깊지 않은지) — 단위 테스트로 노출

### 4. 어댑터의 캐시 갱신 책임 (특별 주목)

`EcoScheduleSaveAdapter`, `EcoCodeSaveAdapter` 가 DB 저장 후 캐시 갱신 (`EcoScheduleCache`).

- DB 저장 성공 → 캐시 갱신 호출 (`InOrder` 검증)
- DB 저장 실패 → 캐시 갱신 **호출되지 않음** (트랜잭션 일관성)
- 캐시 갱신 자체 실패 시 정책 (rollback? 로그만?) — 코드 확인 후 검증

### 5. `EcoScheduleCache`

- 인메모리 캐시 + Redis 백업 (구현 확인)
- get/put/invalidate
- 동시성 (`ConcurrentHashMap` 사용 검증)
- 미존재 key 조회 시 `Optional.empty()` 또는 fallback 정책

### 6. `EcoDynamicIndScheduler`

- 동적 스케줄 등록/취소
- cron 표현식 파싱
- 스케줄 추가/제거 시 Spring `TaskScheduler` 호출 검증
- 단위 테스트로 충분 (실제 시간 흐름 테스트 X)

### 7. JPA Repository / Entity Mapper

- `EconomicIndicatorCode`, `EconomicSchedule`, `EconomicIndicatorValue` (시계열) Entity
- 시계열 테이블이 TimescaleDB hypertable 이면 → `timescale/timescaledb:latest-pg16` 이미지 사용
- 시간 범위 쿼리 정확성

### 8. Kafka Publisher (relay)

토픽명을 **변경 금지** (consumer 영향). 기존 토픽 그대로 사용.

| 데이터 종류 | 토픽 (예상, 코드 확인 필수) |
|---|---|
| 경제지표 시계열 | `economic.indicator-value` 또는 기존 명 |
| 경제 캘린더 | `economic.calendar` |

테스트:
- DB 저장 후 publish 되는 흐름 (통합 테스트, Testcontainers Postgres + Kafka)
- 토픽명 정확성 (코드의 상수 참조)
- payload 구조 (도메인 → Message 매퍼 라운드트립)

---

## fixture 관리

FRED / Investing / Yahoo 응답은 시간 지나면 변하므로:

```
src/test/resources/fixtures/economic/
├── fred/
│   ├── series_GDP_2026-05-01.json
│   └── README.md            # endpoint URL, 호출 일자
├── investing/
│   └── calendar_2026-05-01.html
└── yahoo/
    └── series_AAPL_2026-05-01.json
```

---

## 토픽 / Redis 키

| 종류 | 패턴 |
|---|---|
| Kafka publish | `economic.{indicator-value\|calendar\|...}` (기존 명 그대로) |
| Redis 캐시 | `EcoScheduleCache` 가 사용하는 키 (`RedisKeys` 유틸 확인) |

토픽명은 **코드의 상수 참조**로 검증. 하드코딩된 문자열 비교 금지.

---

## 작업 절차 (이 모듈 한정)

1. `git ls-files modules/economic` 스캔
2. 분류:
   - `economic_ind_shard` 의 베이스 클래스 / Port / 매퍼 / Cache / Scheduler
   - `fred` 어댑터 (HTTP)
   - `crawling` 어댑터 (HTTP)
   - JPA Entity / Repository
   - Kafka Publisher
3. **순서**: `SyncScheduleService<RAW>` 베이스 → 매퍼 → Cache → Scheduler → FRED/Crawling 어댑터 (WireMock) → JPA → Publisher → 통합 테스트 (sync → DB → publish)
4. fixture 디렉토리 정리 + 출처/일자 README 동봉
5. **토픽명을 절대 코드와 다르게 하드코딩하지 말 것** (consumer 영향)

---

## 검증 포인트 (이 모듈 한정)

- [ ] `SyncScheduleService<RAW>` 베이스 — fake RAW 타입으로 6단계 흐름 `InOrder` 검증
- [ ] FRED / Crawling 어댑터 — WireMock fixture 기반 파싱 정확성
- [ ] 어댑터의 DB 저장 후 캐시 갱신 책임 검증 (`InOrder`, 실패 시 캐시 갱신 안 됨)
- [ ] `EcoScheduleCache` 동시성 (`ConcurrentHashMap`)
- [ ] `EcoDynamicIndScheduler` cron 등록/취소
- [ ] JPA Entity 매퍼 라운드트립 + 시계열 시간 범위 쿼리
- [ ] Kafka publish 토픽명을 코드 상수 참조로 검증 (하드코딩 0건)
- [ ] fixture에 수집 일자 / 출처 / endpoint URL 명기
- [ ] `application-test.yml` FRED API key placeholder 처리
- [ ] TimescaleDB 의존 시 `timescale/timescaledb` 이미지 사용
- [ ] 디렉토리 `ingection` 오타 유지, 클래스 패키지 `ingestion` 정상 사용
