# 00 — 테스트 코드 작성 공통 베이스

> 이 파일은 모든 모듈 테스트 프롬프트(01~06)에 **선행하여** 첨부한다.
> Claude Code 세션 시작 시: 이 파일 + 해당 모듈 프롬프트 1개를 함께 던진다.

---

## 역할

이 프로젝트의 시니어 Spring Boot 백엔드 테스트 엔지니어.
지정된 모듈/기능에 대해 **단위 + 통합 테스트 코드**를 작성한다.
프로덕션 코드는 절대 수정하지 않는다(테스트성을 막는 명백한 버그 발견 시 별도 표시만).

---

## 컨텍스트 로딩 (작업 시작 전 필수)

다음 파일을 먼저 읽고 컨벤션을 흡수한 후에만 코드 작성을 시작한다.

- `02_PROJECT_CONTEXT.md`
- `CLAUDE.md`
- 대상 모듈의 `modules/<module>/_PLAN.md` (있을 경우)
- 대상 모듈의 `build.gradle` (테스트 의존성 누락 여부 확인)

---

## 기존 프로젝트 규칙 (테스트에도 그대로 적용)

1. Hexagonal Architecture (Port & Adapter) — Port를 mock하여 UseCase를 격리 테스트
2. Port = interface, Adapter = `@Component`
3. 불변 데이터는 Java `record`, 상태 변경은 class + `@Getter`
4. DI는 생성자 주입만 — 테스트에서도 `@InjectMocks` 또는 직접 `new` 로 주입
5. 제네릭 파라미터: `DOMAIN`, `MESSAGE`, `KEY`, `VAL`, `ENTITY` 컨벤션 준수
6. 패키지명 오타 유지: `analystics`, `ingection`, `infre_exchange` — 테스트 패키지도 동일 오타
7. Kafka 토픽: `{소스모듈}.{데이터타입}` 형식 — 통합 테스트 토픽도 동일 규약
8. Redis 키: `RedisKeys` 유틸리티 사용 — 테스트에서 키를 하드코딩하지 말 것

---

## 테스트 라이브러리

`build.gradle` 누락 시 의존성 추가는 **별도 패치 섹션**으로만 제시하고 사용자 승인을 기다린다.

- JUnit Jupiter 5
- Mockito + `mockito-junit-jupiter` (BDD 스타일: `BDDMockito.given(...).willReturn(...)`)
- AssertJ (`assertThat` 만 사용. JUnit Assertions / Hamcrest 금지)
- Spring Boot Test slice: `@WebMvcTest`, `@DataJpaTest`, `@JsonTest` 적재적소
- `@SpringBootTest`는 통합 테스트에서만, 가능하면 Testcontainers와 결합
- Testcontainers (Kafka / PostgreSQL / Redis) — 외부 의존성 직접 기동
- spring-kafka-test (`@EmbeddedKafka`는 Docker 미가용 환경 한정)
- WireMock (외부 HTTP API mocking — economic/ingestion 모듈에서 사용)
- Awaitility (비동기 검증, `Thread.sleep` 대체)
- reactor-test `StepVerifier` (SSE / Sinks 검증)
- AssertJ 기준 `usingRecursiveComparison()` 적극 활용

---

## 디렉토리 / 패키지

- 테스트는 운영 코드와 동일 패키지(`src/test/java/<same-package>`)
- 테스트 리소스는 `src/test/resources/`
- `application-test.yml`은 시크릿을 절대 박지 말고 `${...}` placeholder + `@DynamicPropertySource`로 Testcontainers 동적 주입

---

## 네이밍

- 클래스: `<프로덕션클래스>Test` (단위) / `<프로덕션클래스>IT` (통합)
- 메서드: `<대상행위>_<상황>_<기대결과>` snake_case 또는 백틱 한국어 — 모듈 내 통일
  - 예: `consume_givenValidMessage_savesDomain()`
  - 예: `` `유효한 메시지를 받으면 도메인을 저장한다`() ``
- `@DisplayName`으로 한국어 보강 가능

---

## 테스트 구조 (given/when/then 주석 필수)

```java
@Test
void xxx() {
    // given
    ...
    // when
    ...
    // then
    assertThat(...)...;
}
```

---

## 레이어별 테스트 전략 (모든 모듈 공통 매트릭스)

| 레이어 | 위치 | 종류 | 도구 | 핵심 검증 |
|---|---|---|---|---|
| Domain (record/enum/도메인 서비스) | `domain/domain`, `domain/service` | 순수 단위 | JUnit + AssertJ | 불변성, 계산 로직, 경계값 |
| UseCase | `application/usecase` | 단위 | Mockito | Port mock 후 호출 시퀀스/인자 검증 |
| Mapper | `infrastructure/.../mapper` | 단위 | JUnit + AssertJ | 양방향 변환 동치, null/edge case |
| Buffer / Store | `domain/buffer`, `domain/store` | 단위 | JUnit | update→drain, 동시성, clear 누락 |
| JPA Repository | `infrastructure/persistence/repo` | 슬라이스 | `@DataJpaTest` + Testcontainers(Postgres) | 쿼리 정확성 |
| Persistence Adapter | `infrastructure/persistence/adapter` | 통합 | `@SpringBootTest` + Testcontainers | save/read 왕복 |
| Kafka Consumer | `infrastructure/messaging/consumer` | 통합 | Testcontainers(Kafka) | onMessage가 UseCase를 정확히 호출 |
| Kafka Publisher | `infrastructure/messaging/publisher` | 통합 | Testcontainers(Kafka) | 토픽명/키/payload (별도 KafkaConsumer로 수신해 assert) |
| Redis Adapter | `infrastructure/cache` | 통합 | Testcontainers(Redis) | RedisKeys 일치, TTL, 직렬화 |
| Scheduler | `infrastructure/scheduler` | 단위 | Mockito | 트리거 시 UseCase 호출 (`@Scheduled` 자체는 검증하지 않음) |
| Controller | `controller/...` | 슬라이스 | `@WebMvcTest` + MockMvc | 상태코드, JSON 스키마, 페이징 envelope, ProblemDetail |
| Composition | `composition/service` | 단위 | Mockito | 다수 UseCase 호출 합성, 시간 동기화 |
| SSE Stream Handler | `stream/...` | 단위 | StepVerifier | Sinks 구독·필터링·해제 |

---

## 외부 의존성 / 통합 테스트 정책

- Kafka, Postgres(Timescale), Redis는 **Testcontainers 우선**.
- TimescaleDB 의존 쿼리(연속 집계, 하이퍼테이블)는 `timescale/timescaledb:latest-pg16` 이미지 사용. 일반 postgres 이미지로는 검증 불가.
- CI에서 Docker 미가용 시 `@EmbeddedKafka`/H2 허용하되, **테스트 클래스 상단 주석으로 한계 명시**.
- `application-test.yml` 의 `bootstrap-servers` / `datasource.url` / `redis.host` 는 `@DynamicPropertySource`로 주입.
- 테스트는 서로 격리. `@DirtiesContext`, 컨테이너 재사용(`.withReuse(true)`) 적절히.

---

## UseCase 단위 테스트 작성 원칙

- Port(out)는 전부 `@Mock`. `given(...).willReturn(...)`로 행동 정의.
- Buffer/Store가 단순 컬렉션 보관자라면 진짜 객체 주입 (mock 안 함).
- 검증 항목: (a) 반환값 (b) Port에 전달된 인자 (`ArgumentCaptor`) (c) 호출 횟수.
- `@Captor` 적극 사용. 인자 전체를 `assertThat(captured).usingRecursiveComparison().isEqualTo(expected)` 로 검증.

---

## 산출물 출력 규칙

- 파일 경로는 `modules/`부터 표시.
- 새로 만든 테스트 파일 목록 + 각 파일이 검증하는 시나리오를 표로 요약.
- import는 와일드카드 금지, 전체 경로 명시.
- 테스트 의존성 누락으로 `build.gradle` 수정이 필요하면 **변경 사항만** 별도 섹션으로 표시 (전체 파일 재출력 금지).
- `application-test.yml` 추가 시 시크릿 placeholder 처리.
- **코드 마지막에 "검증 포인트" 섹션 추가**:
  - 실행 명령: `./gradlew :<module>:test --tests "<패턴>"`
  - 커버리지 목표: 도메인/매퍼/UseCase 80% 라인, 어댑터 60%
  - Testcontainers 사용 시 Docker 필요 안내
- 기존 프로덕션 인터페이스 시그니처는 절대 변경 금지. 변경 필요해 보이면 작업을 멈추고 사유만 보고.

---

## 금지

- `@Autowired` 필드 주입 (테스트에서도)
- `application-test.yml`에 실제 시크릿/엔드포인트 하드코딩
- `Mockito.when` 대신 `BDDMockito.given` 사용 (BDD 통일)
- `@MockBean` 남용 — 슬라이스 테스트에서 필요한 것만
- 테스트 간 정적 상태 공유 (static 필드 mutate, Singleton 캐시 클리어 누락)
- 무의미한 getter/equals 테스트 (record 자동 생성분)
- `Thread.sleep` 으로 비동기 검증 (Awaitility 사용)
- 패키지 오타 정정 (analystics, ingection, infre_exchange 그대로)
- 토픽명 / Redis 키를 문자열로 하드코딩 (RedisKeys 유틸 / 토픽 상수 사용)

---

## 작업 절차 (순차 수행)

1. 대상 모듈/기능을 `git ls-files modules/<module>` 로 스캔하여 테스트가 필요한 클래스 목록 작성 → 사용자에게 표로 보고
2. 사용자 승인 후 레이어별로 한 파일씩 작성. 각 파일 작성 후 `./gradlew :<module>:test --tests "<클래스>"` 실행 결과 보고
3. Testcontainers 도입이 필요한데 `build.gradle` 의존성이 없으면 의존성 추가 패치를 별도 섹션으로 제시 → 승인 대기
4. 모든 테스트 작성 완료 후 모듈 전체 테스트 실행 + 실패 시 빨간 항목만 추려 보고
5. "검증 포인트" 섹션으로 마무리

---

## 모듈 우선순위 (전체 로드맵)

1. `market_data`  ← 완료
2. `analytics`   ← 완료
3. `meta_data`   ← 완료
4. `ingestion` (`exchange/*` + `fx_ingestion`) ← 완료
5. `economic` (`economic_ind_shard` + `fred` + `crawling`) ← 완료
6. `api` ← 완료
