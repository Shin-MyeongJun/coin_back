# User Module Inspection

## P0-P2 Resolution Update (2026-05-18)

P0~P2 리스크는 이번 수정에서 코드 반영 및 targeted test까지 완료했다. 아래 표는 기존 "주요 리스크" 판단에 대한 처리 결과다.

| 우선순위 | 원 리스크 | 처리 결과 |
| --- | --- | --- |
| P0 | API 애플리케이션이 user component/entity/repository를 로딩하지 못함 | `ApiApplication` scan 대상에 `com.example.demo.user`를 포함하고, auto-configuration package 기반으로 JPA repository/entity 탐색 범위를 API/query/user 전체로 정리했다. `UserModuleConfiguration`의 중복 JPA scan 선언은 제거했다. |
| P0 | API 설정에 `ys.auth.jwt.*`가 없음 | API `application.yml`에 `ys.auth.jwt` 설정을 추가하고 운영 값은 환경변수(`JWT_SECRET`, TTL, issuer)에서 받도록 했다. 테스트 리소스에는 별도 안전한 test secret을 추가했다. |
| P0 | user migration은 있으나 API 런타임 Flyway 경로가 없음 | API 모듈에 Flyway 런타임 의존성과 `classpath:db/migration` 설정을 추가했다. |
| P1 | JWT secret 길이/필수 claim 방어 부족 | `JwtProperties`에서 HS256 secret을 32바이트 이상으로 검증하고, verifier가 `jti`, subject, expiration, tier 누락 토큰을 거부하도록 방어했다. |
| P1 | API key 인증 성공 시 `lastUsedAt` 미갱신 | `AuthenticateApiKeyService`에서 성공 인증 시 `touchUsage(now)` 후 저장하고, 저장된 key를 cache에 반영하도록 변경했다. |
| P1 | controller가 Authorization header를 다시 파싱함 | API security filter가 Spring SecurityContext에 `AuthenticatedAccount`/`AuthenticatedApiKey`를 넣도록 하고, `AuthController`/`ApiKeyController`는 `@AuthenticationPrincipal` 기반으로 전환했다. API key로 JWT 전용 endpoint에 접근하지 못하도록 JWT principal authorization을 분리했다. |
| P2 | API key 발급 quota race / prefix collision 처리 약함 | 계정 row를 pessimistic write lock으로 읽은 뒤 quota를 계산하도록 바꾸고, key/account 저장은 `saveAndFlush`로 DB 제약 위반을 즉시 드러내도록 했다. |
| P2 | signup email race에서 DB 예외가 외부로 노출될 수 있음 | 저장 시 `DataIntegrityViolationException`을 `DuplicateEmailException`으로 변환하도록 보강했다. |
| P2 | 미사용 DTO 잔존 | 현재 controller에서 사용하지 않는 `RefreshRequest`, `RevokeApiKeyResponse`를 삭제했다. |

### Verification Update

| 명령 | 결과 | 메모 |
| --- | --- | --- |
| `.\gradlew.bat :user:compileJava :api:compileJava` | 통과 | user/API 통합 컴파일 확인 |
| `.\gradlew.bat :user:test` | 통과 | user 모듈 targeted test 확인 |
| `.\gradlew.bat :api:test` | 통과 | API security/JPA slice test 및 stream controller test까지 확인 |

검토 일시: 2026-05-18

기준 문서: `CLAUDE.md`

대상: `modules/user`

## 결론

`:user` 모듈은 회원가입, 로그인, JWT access/refresh token, API key 발급/인증/폐기, refresh token Redis allowlist, access token blacklist, API key lookup cache를 담당하도록 만들어져 있다. 내부 단위 테스트는 통과하지만, 현재 `:api`와의 실제 런타임 통합은 완성 상태로 보기 어렵다.

가장 큰 문제는 API 앱이 `:user`의 컴포넌트와 엔티티/레포지토리를 스캔하지 않는 점이다. `modules/api/src/main/java/com/example/demo/api/ApiApplication.java`의 `scanBasePackages`, `@EntityScan`, `@EnableJpaRepositories`에는 `com.example.demo.user`가 빠져 있다. 반면 `modules/api/build.gradle`은 `implementation project(':user')`를 추가했고, `SecurityConfig`는 `VerifyAccessTokenUseCase`, `AuthenticateApiKeyUseCase`, `LoadRateLimitPolicyQuery`를 주입받는다. 즉 컴파일은 되지만 실제 API 앱 컨텍스트에서 user bean이 없을 가능성이 높다.

두 번째로, `modules/user/src/main/resources/application-user.yml`은 기본 Spring Boot 설정 파일명이 아니므로 API 앱에서 자동 로드되지 않는다. `ys.auth.jwt.*`, datasource, redis 설정을 API 앱 설정에 병합하거나 `spring.config.import=optional:classpath:application-user.yml` 같은 명시적 import가 필요하다.

세 번째로, `db/migration/V1__account.sql`, `V2__api_key.sql`은 있으나 현재 `build.gradle` 전역 및 `:user`, `:api`에 Flyway dependency가 없다. API의 `spring.jpa.hibernate.ddl-auto`도 `none`이므로 user 테이블 생성/검증 경로가 없다.

## 검증 결과

| 명령 | 결과 | 메모 |
| --- | --- | --- |
| `.\gradlew.bat :user:test` | 통과 | user 모듈 targeted test 성공 |
| `.\gradlew.bat :user:compileJava :api:compileJava` | 통과 | user/API 통합 컴파일 확인 |
| `.\gradlew.bat :api:test` | 통과 | API security/JPA slice test 및 stream controller test 확인 |

초기 점검에서 실패했던 `:api:test`는 API 애플리케이션 JPA 탐색 범위와 stream 보안 테스트 전제를 함께 정리한 뒤 통과했다.

## 주요 리스크 (초기 점검 기준)

아래 표는 최초 점검 당시의 판단이다. P0~P2 항목의 실제 해결 상태는 문서 상단의 "P0-P2 Resolution Update"를 기준으로 본다.

| 우선순위 | 위치 | 내용 | 판단 |
| --- | --- | --- | --- |
| P0 | `ApiApplication` | `:api`가 `:user`를 dependency로만 추가하고 component/entity/repository scan에는 포함하지 않음 | API 인증 필터와 auth/api-key endpoint가 실제 앱에서 bean 미등록으로 깨질 수 있음 |
| P0 | `application-user.yml` | 파일명이 기본 로드 대상이 아니며 API 설정에도 `ys.auth.jwt.*`가 없음 | `JwtProperties` 바인딩, DB/Redis 설정이 운영 앱에 반영되지 않을 수 있음 |
| P0 | `db/migration`, build files | user migration은 있으나 Flyway dependency/config가 없음 | `account`, `api_key` 테이블 생성 경로가 없음 |
| P1 | `JwtProperties`, `JwtTokenIssuerAdapter` | secret 길이 검증이 없음 | 짧은 JWT secret이면 로그인/토큰 발급 시 WeakKey 예외로 런타임 실패 |
| P1 | `JwtTokenVerifierAdapter` | `jti`/expiration null 방어가 부족함 | 서명은 맞지만 필수 claim이 빠진 token에서 NPE가 날 수 있음 |
| P1 | `AuthenticateApiKeyService` | API key 인증 성공 시 `lastUsedAt`을 갱신하지 않음 | DB/entity/response에 있는 `last_used_at`이 사실상 비활성 데이터 |
| P1 | `AuthController`, `ApiKeyController` | 이미 API security filter가 있는데 컨트롤러가 Authorization header를 다시 직접 파싱함 | SecurityContext/request principal 기반으로 정리되지 않아 중복 검증과 정책 불일치 위험 |
| P2 | `IssueApiKeyService` | prefix collision, quota 검사가 DB write와 원자적이지 않음 | 동시 발급 시 unique violation 또는 quota 초과 가능 |
| P2 | `SignupService` | email 중복을 exists 후 save로 확인함 | 동시 가입 race에서 `DuplicateEmailException` 대신 DB 예외가 노출될 수 있음 |
| P2 | `RefreshRequest`, `RevokeApiKeyResponse` | 현재 controller에서 사용되지 않아 삭제됨 | 자동생성 흔적 정리 완료 |

## 기능 흐름

| 기능 | 흐름 |
| --- | --- |
| 회원가입 | `AuthController.signup` -> `SignupService` -> `PasswordEncoderPort` -> `SaveAccountPort` -> `account` |
| 로그인 | `AuthController.login` -> `LoginService` -> password match -> `TokenIssuerPort` -> `RefreshTokenStorePort.allow` |
| refresh | `AuthController.refresh` -> `RefreshTokenService` -> refresh token 검증 -> Redis allowlist 확인/rotation |
| logout | `AuthController.logout` -> `LogoutService` -> access blacklist + refresh allowlist revoke |
| 내 정보 | `AuthController.me` -> `VerifyAccessTokenService` -> token verify + blacklist check + account load |
| API key 발급 | `ApiKeyController.issue` -> `IssueApiKeyService` -> quota/scope 정책 -> secret 생성/hash -> `api_key` save/cache |
| API key 인증 | API `ApiKeyAuthenticationFilter` -> `AuthenticateApiKeyService` -> prefix parse -> cache/DB lookup -> bcrypt match/IP/revoked check |
| API key 목록/폐기 | `ApiKeyController.list/revoke` -> JWT account resolve -> `ListApiKeysService`/`RevokeApiKeyService` |
| rate limit policy | API `RateLimitFilter` -> `LoadRateLimitPolicyService` -> account tier 또는 API key policy |

## 클래스별 기능 매핑

`src/main/java` production class 기준이다. 테스트 클래스는 검증 현황에만 반영했다.

| 클래스 | 레이어 | 연관 기능 | 역할 |
| --- | --- | --- | --- |
| `UserModuleConfiguration` | config | 모듈 통합 | user component scan, `JwtProperties`, JPA repository/entity scan 진입점 |
| `Account` | domain | 계정 | 계정 aggregate, 비밀번호 변경과 tier 변경 상태 관리 |
| `AccountId` | domain | 계정 | UUID 기반 account 식별자 |
| `AccountTier` | domain | 계정/정책 | `FREE`, `PRO`, `ADMIN` 등급 |
| `Email` | domain | 계정 | email 정규화/형식 검증 |
| `PasswordHash` | domain | 계정/인증 | bcrypt hash 값 객체 |
| `ApiKey` | domain | API key | API key aggregate, revoke/touch/scope/IP 허용 판단 |
| `ApiKeyId` | domain | API key | UUID 기반 API key 식별자 |
| `ApiKeyPrefix` | domain | API key | API key lookup용 8자 prefix |
| `ApiKeySecret` | domain | API key | 32자 plaintext secret, 발급 시 1회 노출 |
| `ApiKeyHash` | domain | API key | secret bcrypt hash 값 객체 |
| `ApiKeyScope` | domain | API key 권한 | `READ_MARKET`, `READ_ANALYTICS`, `READ_PRIVATE`, `SSE_STREAM` |
| `ApiKeyIssuanceQuota` | domain | API key 정책 | tier별 최대 key 수 |
| `PrefixAndSecret` | domain | API key 발급 | prefix/secret 생성 결과 |
| `RateLimitPolicy` | domain | rate limit | rpm/rpd/SSE 동시성 정책 값 |
| `RateLimitPolicies` | domain | rate limit | tier별 기본 rate limit |
| `AccessToken` | domain | JWT | access token raw/jti/expiresAt |
| `RefreshToken` | domain | JWT | refresh token raw/jti/accountId/expiresAt |
| `TokenPair` | domain | JWT | access/refresh token 묶음 |
| `AccountFactory` | domain service | 회원가입 | 신규 FREE account 생성 |
| `ApiKeyHasher` | domain service port | API key 보안 | API key secret hash/match 추상화 |
| `ApiKeyIssuancePolicy` | domain service | API key 정책 | tier별 허용 scope, quota, 기본 policy 검증 |
| `DuplicateEmailException` | exception | 회원가입 | 중복 email |
| `InvalidCredentialsException` | exception | 로그인 | email/password 불일치 |
| `TokenInvalidException` | exception | JWT | access/refresh token invalid |
| `AccountNotFoundException` | exception | 계정 | account 없음 |
| `ApiKeyNotFoundException` | exception | API key | key 없음 |
| `ApiKeyOwnershipException` | exception | API key | 타 계정 key 접근 |
| `ApiKeyQuotaExceededException` | exception | API key 정책 | tier별 quota 초과 |
| `ApiKeyScopeNotAllowedException` | exception | API key 정책 | tier에서 허용하지 않는 scope |
| `SignupUseCase` | port/in | 회원가입 | signup 입력 port |
| `LoginUseCase` | port/in | 로그인 | login 입력 port |
| `RefreshTokenUseCase` | port/in | JWT refresh | refresh 입력 port |
| `LogoutUseCase` | port/in | logout | logout 입력 port |
| `VerifyAccessTokenUseCase` | port/in | JWT 인증 | Bearer token 검증 입력 port |
| `GetCurrentAccountQuery` | port/in | 내 정보 | account 조회 입력 port |
| `IssueApiKeyUseCase` | port/in | API key 발급 | key 발급 입력 port |
| `ListApiKeysQuery` | port/in | API key 목록 | 계정별 key 목록 입력 port |
| `RevokeApiKeyUseCase` | port/in | API key 폐기 | key revoke 입력 port |
| `AuthenticateApiKeyUseCase` | port/in | API key 인증 | Authorization ApiKey 인증 입력 port |
| `LoadRateLimitPolicyQuery` | port/in | rate limit | account/API key 정책 조회 입력 port |
| `AuthenticatedAccount` | port/in DTO | JWT 인증 | 검증된 계정 principal |
| `AuthenticatedApiKey` | port/in DTO | API key 인증 | 검증된 API key principal |
| `IssuedApiKey` | port/in DTO | API key 발급 | 저장된 key + 1회성 plaintext secret |
| `LoadAccountPort` | port/out | 계정 persistence | account load/existence 조회 |
| `SaveAccountPort` | port/out | 계정 persistence | account save |
| `PasswordEncoderPort` | port/out | 비밀번호 보안 | password encode/match |
| `TokenIssuerPort` | port/out | JWT 발급 | token pair 발급 |
| `TokenVerifierPort` | port/out | JWT 검증 | access/refresh token claim 검증 |
| `RefreshTokenStorePort` | port/out | Redis refresh | refresh allowlist allow/revoke |
| `AccessTokenBlacklistPort` | port/out | Redis logout | access token blacklist |
| `LoadApiKeyPort` | port/out | API key persistence | key id/account 조회 및 active count |
| `LoadApiKeyByPrefixPort` | port/out | API key 인증 | prefix 기반 lookup |
| `SaveApiKeyPort` | port/out | API key persistence | key save |
| `ApiKeyLookupCachePort` | port/out | API key cache | prefix lookup cache get/put/evict |
| `ApiKeySecretGeneratorPort` | port/out | API key 발급 | prefix/secret 생성 |
| `VerifiedClaims` | port/out DTO | JWT 검증 | verifier가 반환하는 accountId/jti/tier/expiry |
| `SignupService` | usecase | 회원가입 | 중복 email 검사, password hash, account 저장 |
| `LoginService` | usecase | 로그인 | password match, token 발급, refresh allowlist 저장 |
| `RefreshTokenService` | usecase | JWT refresh | refresh 검증, allowlist 확인, rotation |
| `LogoutService` | usecase | logout | access blacklist, refresh revoke |
| `VerifyAccessTokenService` | usecase | JWT 인증 | access token 검증, blacklist 확인, account load |
| `GetCurrentAccountService` | usecase | 내 정보 | accountId로 account 조회 |
| `IssueApiKeyService` | usecase | API key 발급 | scope/quota 검증, prefix/secret 생성, hash, 저장/cache |
| `AuthenticateApiKeyService` | usecase | API key 인증 | header parse, cache/DB lookup, bcrypt/IP/revoke 검증 |
| `ListApiKeysService` | usecase | API key 목록 | 계정별 key 목록 |
| `RevokeApiKeyService` | usecase | API key 폐기 | 소유권 확인, revoke, cache evict |
| `LoadRateLimitPolicyService` | usecase | rate limit | account tier/API key별 rate policy 반환 |
| `AccountEntity` | persistence entity | 계정 DB | `account` table mapping |
| `ApiKeyEntity` | persistence entity | API key DB | `api_key` table mapping |
| `AccountMapper` | persistence mapper | 계정 DB | account entity/domain 변환 |
| `ApiKeyMapper` | persistence mapper | API key DB | api key entity/domain 변환, array scope/IP mapping |
| `AccountJpaRepository` | persistence repo | 계정 DB | email/id 조회, email 존재 확인 |
| `ApiKeyJpaRepository` | persistence repo | API key DB | prefix/id/account 조회, active count |
| `LoadAccountAdapter` | persistence adapter | 계정 DB | `LoadAccountPort` 구현 |
| `SaveAccountAdapter` | persistence adapter | 계정 DB | `SaveAccountPort` 구현 |
| `LoadApiKeyAdapter` | persistence adapter | API key DB | `LoadApiKeyPort` 구현 |
| `LoadApiKeyByPrefixAdapter` | persistence adapter | API key 인증 | `LoadApiKeyByPrefixPort` 구현 |
| `SaveApiKeyAdapter` | persistence adapter | API key DB | `SaveApiKeyPort` 구현 |
| `BCryptPasswordEncoderAdapter` | security adapter | 비밀번호 보안 | BCrypt password encoder |
| `BCryptApiKeyHasherAdapter` | security adapter | API key 보안 | BCrypt API key secret hasher |
| `SecureRandomApiKeySecretGenerator` | security adapter | API key 발급 | SecureRandom prefix/secret 생성 |
| `JwtProperties` | security config | JWT | `ys.auth.jwt` 설정 바인딩 |
| `JwtTokenIssuerAdapter` | security adapter | JWT 발급 | HS256 access/refresh JWT 생성 |
| `JwtTokenVerifierAdapter` | security adapter | JWT 검증 | issuer/type/exp/tier/account claim 검증 |
| `RedisRefreshTokenStoreAdapter` | cache adapter | JWT/logout | refresh allowlist, access blacklist Redis 구현 |
| `RedisApiKeyLookupCacheAdapter` | cache adapter | API key 인증 | API key prefix lookup Redis cache |
| `AuthController` | web | auth API | `/api/v1/auth/signup/login/refresh/logout/me` |
| `ApiKeyController` | web | API key API | `/api/v1/api-keys` 발급/목록/폐기 |
| `UserErrorAdvice` | web error | auth/API key API | user exception -> RFC 7807 ProblemDetail |
| `SignupRequest` | web DTO | 회원가입 | signup request validation |
| `SignupResponse` | web DTO | 회원가입 | signup response |
| `LoginRequest` | web DTO | 로그인 | login request validation |
| `TokenResponse` | web DTO | 로그인/refresh | access token response |
| `MeResponse` | web DTO | 내 정보 | current account response |
| `IssueApiKeyRequest` | web DTO | API key 발급 | label/scopes/IP allowlist request |
| `IssueApiKeyResponse` | web DTO | API key 발급 | secret 포함 1회성 issue response |
| `ApiKeySummaryResponse` | web DTO | API key 목록 | secret/hash 제외 key summary |
| `PolicyResponse` | web DTO | API key/rate limit | rpm/rpd/sseConcurrent response |

## 추가 판단 포인트

1. `:user`를 API 앱에 붙일지, 별도 auth service로 분리할지 먼저 결정해야 한다. 현재 코드는 API에 붙이는 형태로 절반 정도 진행되어 있다.
2. API에 붙인다면 `ApiApplication` scan/import, user 설정 import, Flyway dependency/location, API 보안 필터와 user controller의 principal 전달 방식을 같이 정리해야 한다.
3. MVP 범위가 public read 중심이라면 `AuthController`, `ApiKeyController`, JWT/Redis/Flyway까지 한 번에 넣는 것은 범위가 커진다. 인증/API key만 최소 기능으로 살릴지, watchlist/alert까지 확장할지 분리 판단이 필요하다.
4. `CLAUDE.md`에는 현재 `:user`, `:alert` 모듈이 반영되어 있지 않다. 이 모듈을 유지한다면 source of truth 문서 갱신이 필요하다.
