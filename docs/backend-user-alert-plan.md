# Backend User / Watchlist / Alert Plan

> 작성 목적: 프런트엔드는 별도 프로젝트에서 진행하고, 이 백엔드 저장소에는 사용자 인증, 권한, Watchlist, AlertRule, AlertHistory 기능을 기존 구조에 맞춰 추가하기 위한 상세 구현 계획입니다.

---

## 0. 결론: 새 모듈이 기존 모듈에 주는 영향

영향을 작게 유지할 수 있습니다.

권장 구조는 새 기능을 `:user`, `:alert` 두 모듈로 분리하고, 기존 `:api` 모듈만 이 새 모듈들을 호출하게 만드는 방식입니다.

```text
contracts
  <- infra_shard
  <- query/*
  <- user
  <- alert
  <- api
```

기존 write-side 파이프라인에는 의존성을 추가하지 않습니다.

```text
ingestion -> market_data -> analytics -> query -> api
```

위 흐름은 그대로 둡니다.

### 영향이 없는 영역

아래 모듈은 새 사용자/알림 기능을 위해 직접 수정하지 않는 것을 원칙으로 합니다.

- `:contracts`
- `:meta_data`
- `:market_data`
- `:analytics`
- `:market_data_query`
- `:meta_data_query`
- `:analytics_query`
- `:economic_query`
- ingestion 계열 모듈
- infra exchange 계열 모듈

특히 Kafka topic, market/premium message record, Redis latest key, market data 저장 로직은 건드리지 않습니다.

### 영향이 생기는 영역

최소 변경 지점은 다음입니다.

| 파일/영역 | 변경 이유 | 영향 |
| --- | --- | --- |
| `settings.gradle` | `:user`, `:alert` 모듈 추가 | Gradle compile 대상 증가 |
| `modules/api/build.gradle` | `project(':user')`, `project(':alert')` 의존 추가 | API가 새 usecase 호출 가능 |
| `modules/api/ApiApplication.java` | component/entity/repository scan 범위 추가 | user/alert bean과 JPA entity 인식 |
| `modules/api/config/SecurityConfig.java` | `permitAll`에서 JWT 기반 보호 경로 추가 | 보안 정책 변화 |
| `modules/api/controller/*` | auth/watchlist/alert API controller 추가 | 새 endpoint 노출 |
| DB schema | user/watchlist/alert table 추가 | 운영 DB migration 필요 |

### 가장 큰 리스크

`SecurityConfig` 변경입니다.

기존 read-only API와 SSE는 프런트 개발 중 바로 호출되어야 하므로, 처음부터 전체 API를 인증 필수로 바꾸지 않습니다.

권장 정책:

```text
permitAll:
  GET /api/v1/meta/**
  GET /api/v1/market/**
  GET /api/v1/analytics/**
  GET /api/v1/economic/**
  GET /api/v1/compose/**
  GET /api/v1/stream/**
  POST /api/v1/auth/register
  POST /api/v1/auth/login
  /actuator/health

authenticated:
  /api/v1/me
  /api/v1/watchlist/**
  /api/v1/alerts/**

ADMIN:
  /api/v1/admin/**
```

이렇게 하면 기존 프런트/조회 API와 SSE에는 영향이 거의 없습니다.

---

## 1. 모듈 구성

### 1.1 `:user`

책임:

- 사용자 계정
- 비밀번호 해시
- 역할/권한
- 로그인 검증
- JWT 생성/검증에 필요한 도메인 서비스

경로:

```text
modules/user
```

패키지:

```text
com.example.demo.user
```

주의:

- DB table 이름은 `user`를 피하고 `app_user` 사용.
- 인증은 API 진입점에서 수행하지만, 계정과 토큰 usecase는 `:user`에 둡니다.

### 1.2 `:alert`

책임:

- Watchlist
- AlertRule
- AlertTriggerHistory
- Alert evaluator domain service
- active rule cache/store

경로:

```text
modules/alert
```

패키지:

```text
com.example.demo.alert
```

주의:

- `:alert`는 `:api`에 의존하지 않습니다.
- `:api`가 SSE/Kafka stream을 받아 `:alert`의 evaluate usecase를 호출합니다.
- 1차 구현에서는 Redis active rule cache를 넣지 않고 in-memory store + DB refresh로 시작합니다. 그러면 `infra_shard.RedisKeys` 변경 없이 진행할 수 있습니다.

---

## 2. Gradle 작업

### 2.1 `settings.gradle`

추가:

```gradle
include ':user', ':alert'

project(':user').projectDir = file('modules/user')
project(':alert').projectDir = file('modules/alert')
```

기존 include 목록에 자연스럽게 끼워 넣습니다.

### 2.2 `modules/user/build.gradle`

예시:

```gradle
plugins {
    id 'java'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.security:spring-security-crypto'
    implementation 'com.auth0:java-jwt:4.5.0'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 2.3 `modules/alert/build.gradle`

QueryDSL을 쓰면 검색/필터 구현이 기존 query 모듈과 잘 맞습니다.

```gradle
plugins {
    id 'java'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 2.4 `modules/api/build.gradle`

추가:

```gradle
implementation project(':user')
implementation project(':alert')
```

이미 `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-security-test`가 있으므로 API 쪽 추가 의존은 최소화합니다.

---

## 3. API Application Scan

`modules/api/src/main/java/com/example/demo/api/ApiApplication.java`

추가 대상:

```java
@SpringBootApplication(scanBasePackages = {
        "com.example.demo.api",
        "com.example.demo.analytics_query",
        "com.example.demo.meta_data_query",
        "com.example.demo.market_data_query",
        "com.example.demo.economic_query",
        "com.example.demo.infra_shard",
        "com.example.demo.user",
        "com.example.demo.alert"
})
@EntityScan(basePackages = {
        "com.example.demo.analytics_query.infrastructure.persistence.entity",
        "com.example.demo.meta_data_query.infrastructure.persistence.entity",
        "com.example.demo.market_data_query.infrastructure.persistence.entity",
        "com.example.demo.economic_query.infrastructure.persistence.entity",
        "com.example.demo.user.infrastructure.persistence.entity",
        "com.example.demo.alert.infrastructure.persistence.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.example.demo.analytics_query.infrastructure.persistence.repo",
        "com.example.demo.meta_data_query.infrastructure.persistence.repo",
        "com.example.demo.market_data_query.infrastructure.persistence.repo",
        "com.example.demo.economic_query.infrastructure.persistence.repo",
        "com.example.demo.user.infrastructure.persistence.repo",
        "com.example.demo.alert.infrastructure.persistence.repo"
})
```

---

## 4. DB Schema

현재 API `application.yml`은 `spring.jpa.hibernate.ddl-auto: none`입니다. 따라서 테이블은 자동 생성되지 않습니다.

개발 중 선택지는 둘 중 하나입니다.

1. local에서만 `JPA_DDL_AUTO=update`로 띄운다.
2. SQL schema 파일을 작성해서 DB에 적용한다.

운영/문서 기준으로는 SQL schema를 남기는 쪽을 권장합니다.

권장 파일:

```text
docs/sql/user_alert_schema.sql
```

### 4.1 `app_user`

```sql
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_app_user_status ON app_user(status);
```

### 4.2 `watchlist_item`

```sql
CREATE TABLE watchlist_item (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    market_code_id BIGINT NOT NULL,
    symbol VARCHAR(50),
    domestic_exchange_id BIGINT,
    offshore_exchange_id BIGINT,
    display_order INTEGER NOT NULL DEFAULT 0,
    memo VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_watchlist_user_market UNIQUE (user_id, market_code_id)
);

CREATE INDEX idx_watchlist_user_order ON watchlist_item(user_id, display_order, id);
```

외래키는 초기에 강제하지 않아도 됩니다. 기존 query table과 계정 table 생명주기가 다르므로, MVP에서는 `user_id` 값 기준으로 application-level ownership 검증을 우선합니다.

### 4.3 `alert_rule`

```sql
CREATE TABLE alert_rule (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    metric VARCHAR(50) NOT NULL,
    operator VARCHAR(20) NOT NULL,
    threshold NUMERIC(30, 10) NOT NULL,
    symbol VARCHAR(50),
    market_code_id BIGINT,
    base_exchange_id BIGINT,
    compare_exchange_id BIGINT,
    interval VARCHAR(20),
    enabled BOOLEAN NOT NULL,
    cooldown_seconds INTEGER NOT NULL,
    last_triggered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_alert_rule_user_enabled ON alert_rule(user_id, enabled);
CREATE INDEX idx_alert_rule_target_enabled ON alert_rule(target_type, enabled);
CREATE INDEX idx_alert_rule_symbol ON alert_rule(symbol);
```

### 4.4 `alert_trigger_history`

```sql
CREATE TABLE alert_trigger_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_rule_id BIGINT NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    metric VARCHAR(50) NOT NULL,
    observed_value NUMERIC(30, 10) NOT NULL,
    threshold NUMERIC(30, 10) NOT NULL,
    message VARCHAR(500) NOT NULL,
    snapshot_json TEXT,
    triggered_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_alert_history_user_id_desc ON alert_trigger_history(user_id, id DESC);
CREATE INDEX idx_alert_history_rule_id_desc ON alert_trigger_history(alert_rule_id, id DESC);
CREATE INDEX idx_alert_history_triggered_at ON alert_trigger_history(triggered_at DESC);
```

---

## 5. `:user` 상세 구조

```text
modules/user/src/main/java/com/example/demo/user/
  domain/
    AppUser.java
    UserRole.java
    UserStatus.java
    AuthToken.java
  application/
    port/in/
      RegisterUserUseCase.java
      LoginUseCase.java
      GetCurrentUserUseCase.java
      VerifyJwtUseCase.java
    port/out/
      LoadUserPort.java
      SaveUserPort.java
      PasswordHashPort.java
      JwtTokenPort.java
    usecase/
      RegisterUserService.java
      LoginService.java
      GetCurrentUserService.java
      VerifyJwtService.java
  infrastructure/
    persistence/
      entity/
        AppUserEntity.java
      mapper/
        AppUserEntityMapper.java
      repo/
        AppUserJpaRepository.java
      adapter/
        LoadUserAdapter.java
        SaveUserAdapter.java
    security/
      BCryptPasswordHashAdapter.java
      JwtProperties.java
      JavaJwtTokenAdapter.java
```

### 5.1 Domain

`AppUser`

```java
public record AppUser(
        Long id,
        String email,
        String passwordHash,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
```

`UserRole`

```java
public enum UserRole {
    USER,
    ADMIN
}
```

`UserStatus`

```java
public enum UserStatus {
    ACTIVE,
    LOCKED,
    DELETED
}
```

`AuthToken`

```java
public record AuthToken(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {}
```

### 5.2 UseCase

`RegisterUserUseCase`

```java
AuthToken register(String email, String rawPassword);
```

`LoginUseCase`

```java
AuthToken login(String email, String rawPassword);
```

`GetCurrentUserUseCase`

```java
AppUser get(Long userId);
```

`VerifyJwtUseCase`

```java
AuthenticatedUser verify(String token);
```

`AuthenticatedUser`

```java
public record AuthenticatedUser(
        Long userId,
        String email,
        UserRole role
) {}
```

---

## 6. `:alert` 상세 구조

```text
modules/alert/src/main/java/com/example/demo/alert/
  domain/
    WatchlistItem.java
    AlertRule.java
    AlertTriggerHistory.java
    AlertTargetType.java
    AlertMetric.java
    AlertOperator.java
    AlertEvaluationResult.java
  domain/service/
    AlertEvaluator.java
    AlertCooldownPolicy.java
  application/
    dto/
      AlertRuleSearchCondition.java
      AlertHistoryCursor.java
    port/in/
      AddWatchlistItemUseCase.java
      RemoveWatchlistItemUseCase.java
      SearchWatchlistUseCase.java
      CreateAlertRuleUseCase.java
      UpdateAlertRuleUseCase.java
      DeleteAlertRuleUseCase.java
      SearchAlertRuleUseCase.java
      SearchAlertHistoryUseCase.java
      EvaluatePremiumAlertUseCase.java
    port/out/
      LoadWatchlistPort.java
      SaveWatchlistPort.java
      DeleteWatchlistPort.java
      LoadAlertRulePort.java
      SaveAlertRulePort.java
      DeleteAlertRulePort.java
      LoadAlertHistoryPort.java
      SaveAlertHistoryPort.java
      ActiveAlertRuleStorePort.java
    usecase/
      WatchlistService.java
      AlertRuleService.java
      AlertHistoryService.java
      EvaluatePremiumAlertService.java
  infrastructure/
    persistence/
      entity/
        WatchlistItemEntity.java
        AlertRuleEntity.java
        AlertTriggerHistoryEntity.java
      mapper/
        WatchlistItemEntityMapper.java
        AlertRuleEntityMapper.java
        AlertTriggerHistoryEntityMapper.java
      repo/
        WatchlistItemJpaRepository.java
        AlertRuleJpaRepository.java
        AlertTriggerHistoryJpaRepository.java
      querydsl/
        WatchlistQueryDslRepository.java
        AlertRuleQueryDslRepository.java
        AlertHistoryQueryDslRepository.java
      adapter/
        WatchlistPersistenceAdapter.java
        AlertRulePersistenceAdapter.java
        AlertHistoryPersistenceAdapter.java
    cache/
      InMemoryActiveAlertRuleStore.java
    scheduler/
      ActiveAlertRuleRefreshScheduler.java
```

### 6.1 Alert Target

1차 대상:

```java
public enum AlertTargetType {
    PREMIUM,
    TICK,
    INDICATOR
}
```

1차 구현은 `PREMIUM`부터 시작합니다. `TICK`, `INDICATOR`는 타입만 열어두고 evaluator는 나중에 붙입니다.

### 6.2 Alert Metric

```java
public enum AlertMetric {
    BUY_PREMIUM_RATE,
    SELL_PREMIUM_RATE,
    LAST_PRICE,
    RSI,
    MACD,
    BOLLINGER_UPPER,
    BOLLINGER_LOWER
}
```

프런트 요구사항 때문에 premium은 반드시 buy/sell을 나눕니다.

### 6.3 Alert Operator

```java
public enum AlertOperator {
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    CROSSES_ABOVE,
    CROSSES_BELOW
}
```

MVP evaluator에서는 비교 연산 4개만 먼저 구현하고, cross 계열은 history/state가 필요하므로 2차로 미룹니다.

---

## 7. API Controller 계획

API controller는 `:api` 모듈에 둡니다.

```text
modules/api/src/main/java/com/example/demo/api/controller/auth/
  AuthController.java
  dto/
    RegisterRequest.java
    LoginRequest.java
    AuthTokenResponse.java
    CurrentUserResponse.java

modules/api/src/main/java/com/example/demo/api/controller/watchlist/
  WatchlistController.java
  dto/
    AddWatchlistItemRequest.java
    WatchlistItemResponse.java

modules/api/src/main/java/com/example/demo/api/controller/alert/
  AlertRuleController.java
  AlertHistoryController.java
  dto/
    CreateAlertRuleRequest.java
    UpdateAlertRuleRequest.java
    AlertRuleResponse.java
    AlertHistoryResponse.java
```

### 7.1 Auth endpoints

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/me
```

`GET /api/v1/me`는 authenticated 필요.

### 7.2 Watchlist endpoints

```text
GET    /api/v1/watchlist?keyword=&page=&size=
POST   /api/v1/watchlist
DELETE /api/v1/watchlist/{watchlistItemId}
```

응답은 기존 `OffsetPage<T>` 재사용.

### 7.3 AlertRule endpoints

```text
GET    /api/v1/alerts/rules?keyword=&targetType=&metric=&enabled=&page=&size=
POST   /api/v1/alerts/rules
PUT    /api/v1/alerts/rules/{ruleId}
PATCH  /api/v1/alerts/rules/{ruleId}/enabled
DELETE /api/v1/alerts/rules/{ruleId}
```

응답은 기존 `OffsetPage<T>` 재사용.

### 7.4 AlertHistory endpoints

```text
GET /api/v1/alerts/history?cursor=&limit=&ruleId=
```

응답은 기존 `CursorPage<T>` 재사용.

MVP cursor는 `id DESC` 기준으로 둡니다. `nextCursor`는 다음 페이지의 마지막 id입니다.

---

## 8. Security 구현 계획

### 8.1 API security package

```text
modules/api/src/main/java/com/example/demo/api/security/
  AuthenticatedUserPrincipal.java
  JwtAuthenticationFilter.java
  JwtAuthenticationEntryPoint.java
  AccessDeniedHandlerImpl.java
```

`JwtAuthenticationFilter`는 `Authorization: Bearer <token>`을 읽고 `VerifyJwtUseCase`를 호출합니다.

### 8.2 SecurityConfig

기본 정책:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
    .requestMatchers(HttpMethod.GET,
            "/api/v1/meta/**",
            "/api/v1/market/**",
            "/api/v1/analytics/**",
            "/api/v1/economic/**",
            "/api/v1/compose/**",
            "/api/v1/stream/**").permitAll()
    .requestMatchers("/actuator/health").permitAll()
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

주의:

- `GET /api/v1/stream/**`은 처음에는 permitAll로 둡니다.
- 나중에 사용자별 private alert stream이 생기면 `/api/v1/alerts/stream` 같은 별도 authenticated endpoint로 추가합니다.

---

## 9. Alert Evaluation 계획

### 9.1 1차: CRUD only

먼저 AlertRule CRUD와 History 조회까지만 구현합니다.

장점:

- 기존 Kafka/SSE 영향 없음
- 보안/권한/CRUD/search/pagination 검증 가능
- 프런트가 Alert UI를 만들 수 있는 API 계약 확보

### 9.2 2차: API stream bridge

기존 `api` 모듈은 이미 Kafka premium topic을 소비해 `MarketDataStream.premiumSink`로 흘립니다.

새 Kafka consumer를 만들지 않고, API 내부에서 sink를 구독해 alert evaluator로 넘깁니다.

```text
Kafka market-data.premium
  -> PremiumStreamConsumer
  -> MarketDataStream.premiumSink
  -> PremiumAlertBridge
  -> EvaluatePremiumAlertUseCase
  -> AlertEvaluator
  -> AlertTriggerHistory save
```

API bridge 파일:

```text
modules/api/src/main/java/com/example/demo/api/alert/PremiumAlertBridge.java
```

property:

```yaml
app:
  alert:
    evaluator:
      enabled: ${ALERT_EVALUATOR_ENABLED:false}
```

초기 기본값은 `false`를 권장합니다. CRUD 검증 후 켭니다.

### 9.3 중복 발화 리스크

현재 API Kafka stream consumer는 UUID group id를 사용합니다. API 인스턴스가 여러 개면 모든 인스턴스가 같은 premium event를 받아 alert를 중복 발화할 수 있습니다.

MVP 단일 API 인스턴스에서는 허용 가능합니다.

운영 보강 후보:

- `:alert_worker` 별도 실행 모듈
- stable consumer group
- Redis distributed lock
- DB unique event key
- outbox pattern

---

## 10. 검색 / 페이지네이션 기준

### 10.1 Offset pagination

대상:

- Watchlist
- AlertRule

응답:

```java
OffsetPage<T>
```

QueryDSL에서 `offset`, `limit`, `total`을 계산합니다.

### 10.2 Cursor pagination

대상:

- AlertHistory

응답:

```java
CursorPage<T>
```

초기 기준:

```text
ORDER BY id DESC
cursor = lastSeenId
WHERE id < cursor
```

나중에 event time 기반으로 바꾸고 싶으면 `(triggeredAt, id)` composite cursor를 별도 response로 확장해야 합니다.

---

## 11. 구현 순서

### Step 0. 작업 전 확인

```powershell
git status --short
```

이 작업은 10개 이상 파일을 추가/수정할 가능성이 높습니다. 별도 브랜치 권장:

```powershell
git switch -c codex/user-alert-backend
```

단, 기존 dirty 변경이 있으면 먼저 범위를 확인하고 충돌 가능성을 보고합니다.

### Step 1. 모듈 skeleton

- `modules/user/build.gradle`
- `modules/alert/build.gradle`
- `settings.gradle` include 추가
- 빈 package와 placeholder test 추가

검증:

```powershell
.\gradlew.bat :user:compileJava
.\gradlew.bat :alert:compileJava
```

### Step 2. User domain/persistence/usecase

구현:

- `AppUser`
- `UserRole`
- `UserStatus`
- `AuthToken`
- `AppUserEntity`
- `AppUserJpaRepository`
- mapper
- load/save adapters
- register/login/current user usecases
- password hash adapter
- JWT adapter

테스트:

- duplicate email
- password mismatch
- locked/deleted user login denied
- JWT create/verify

검증:

```powershell
.\gradlew.bat :user:test
```

### Step 3. API auth/security

구현:

- Auth controller
- Auth request/response DTO
- JWT filter
- SecurityConfig authorization rules
- `ApiApplication` scan 추가

테스트:

- register returns token
- login returns token
- `/api/v1/me` without token returns 401
- `/api/v1/me` with token returns current user
- existing read endpoint remains 200 without token

검증:

```powershell
.\gradlew.bat :api:test
```

### Step 4. Watchlist

구현:

- domain
- entity/repo/mapper
- QueryDSL search
- add/remove/search usecases
- WatchlistController

규칙:

- user ownership 검증
- duplicate `(userId, marketCodeId)` 방지
- offset pagination

테스트:

- add item
- duplicate rejected
- remove own item
- cannot remove another user's item
- search/filter/page

### Step 5. AlertRule CRUD/search/page

구현:

- AlertRule domain
- enum
- entity/repo/mapper
- QueryDSL search
- create/update/delete/enable usecases
- AlertRuleController

검증 규칙:

- threshold required
- cooldownSeconds >= 0
- PREMIUM target은 `BUY_PREMIUM_RATE` 또는 `SELL_PREMIUM_RATE` 허용
- target 식별자는 최소 하나 필요: `marketCodeId` 또는 `(symbol, baseExchangeId, compareExchangeId)`
- user ownership 검증

테스트:

- create premium rule
- invalid metric rejected
- update own rule
- cannot update another user's rule
- enable/disable
- search by enabled/metric/keyword

### Step 6. AlertHistory cursor page

구현:

- AlertTriggerHistory domain
- entity/repo/mapper
- cursor query
- AlertHistoryController

테스트:

- first page
- next page by cursor
- filter by ruleId
- user ownership

### Step 7. Premium evaluator

구현:

- `AlertEvaluator`
- `AlertCooldownPolicy`
- `InMemoryActiveAlertRuleStore`
- `ActiveAlertRuleRefreshScheduler`
- `EvaluatePremiumAlertUseCase`
- `PremiumAlertBridge` in API

초기 지원:

- `BUY_PREMIUM_RATE`
- `SELL_PREMIUM_RATE`
- `GREATER_THAN`
- `GREATER_THAN_OR_EQUAL`
- `LESS_THAN`
- `LESS_THAN_OR_EQUAL`

주의:

- `CROSSES_ABOVE`, `CROSSES_BELOW`는 previous value state가 필요하므로 MVP에서는 reject하거나 disabled 처리합니다.
- `snapshot_json`에는 원본 `PremiumMessage` 요약을 저장합니다.

테스트:

- threshold match creates history
- threshold mismatch does not create history
- cooldown prevents repeated trigger
- disabled rule ignored

### Step 8. 문서 갱신

갱신 대상:

- `CLAUDE.md`
- API README 또는 별도 docs
- SQL schema doc

문서에 명시:

- 기존 market data pipeline 영향 없음
- 새 보호 endpoint 목록
- alert evaluator는 기본 disabled
- multi-instance 중복 발화 리스크

### Step 9. 최종 검증

기본:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat compileTestJava
```

targeted:

```powershell
.\gradlew.bat :user:test
.\gradlew.bat :alert:test
.\gradlew.bat :api:test
```

전체 test는 Kafka/PostgreSQL/Redis/Testcontainers 상태에 따라 실패할 수 있으므로, 실패 시 모듈별로 분리합니다.

---

## 12. API Request / Response 초안

### Register

```json
POST /api/v1/auth/register
{
  "email": "user@example.com",
  "password": "password1234"
}
```

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600
}
```

### Login

```json
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "password1234"
}
```

### Create Watchlist Item

```json
POST /api/v1/watchlist
{
  "marketCodeId": 1,
  "symbol": "BTC",
  "domesticExchangeId": 1,
  "offshoreExchangeId": 2,
  "displayOrder": 0,
  "memo": "BTC kimchi premium"
}
```

### Create AlertRule

```json
POST /api/v1/alerts/rules
{
  "name": "BTC buy premium above 5%",
  "targetType": "PREMIUM",
  "metric": "BUY_PREMIUM_RATE",
  "operator": "GREATER_THAN_OR_EQUAL",
  "threshold": 5.0,
  "symbol": "BTC",
  "baseExchangeId": 1,
  "compareExchangeId": 2,
  "enabled": true,
  "cooldownSeconds": 300
}
```

### AlertHistory Page

```json
GET /api/v1/alerts/history?cursor=1000&limit=20
```

```json
{
  "items": [
    {
      "id": 999,
      "ruleId": 10,
      "targetType": "PREMIUM",
      "metric": "BUY_PREMIUM_RATE",
      "observedValue": 5.12,
      "threshold": 5.0,
      "message": "BTC buy premium is 5.12%",
      "triggeredAt": 1760000000000
    }
  ],
  "nextCursor": 980,
  "hasMore": true
}
```

---

## 13. 하지 말아야 할 것

- 기존 premium 계산 로직을 이 작업에 섞지 않습니다.
- 기존 Kafka topic 이름을 바꾸지 않습니다.
- 기존 `market-data.premium` message record를 바꾸지 않습니다.
- read-side query 모듈이 `:user` 또는 `:alert`에 의존하게 만들지 않습니다.
- `:alert`가 `:api`에 의존하게 만들지 않습니다.
- 첫 단계부터 전체 API를 인증 필수로 바꾸지 않습니다.
- Redis cache부터 시작하지 않습니다. DB + in-memory store로 먼저 검증합니다.
- alert evaluator를 기본 enabled로 시작하지 않습니다.

---

## 14. 작업 크기 추정

| 범위 | 예상 |
| --- | ---: |
| 모듈 skeleton + Gradle | 1~2h |
| user/auth/JWT | 6~10h |
| API security/controller | 4~7h |
| watchlist CRUD/search/page | 5~8h |
| alert rule CRUD/search/page | 7~10h |
| alert history cursor page | 3~5h |
| premium evaluator bridge | 6~10h |
| 테스트/문서/컴파일 정리 | 6~10h |

CRUD/search/page/auth까지만 하면 20~30h.

실시간 premium evaluator와 history까지 포함하면 32~50h.

