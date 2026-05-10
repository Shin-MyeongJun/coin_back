# Agent Instructions

이 문서는 TradeJournal 프로젝트에서 AI 에이전트와 개발자가 따를 작업 기준입니다.

TradeJournal은 전략 저장, 백테스트 결과 리포트화, 공유, 필터링을 중심으로 한 투자 리서치/매매 기록 서비스입니다. 기존 CoinData 프로젝트의 코드 규칙과 도메인 분리 감각은 유지하되, 구조는 사용자 중심 CRUD 서비스에 맞게 더 가볍게 가져갑니다.

## Source Of Truth

작업을 시작할 때 먼저 아래 문서를 읽습니다.

1. `ARCHITECTURE.md`
2. `REPORT_TEMPLATE.md`가 있다면 함께 확인
3. `DATA_POLICY.md`가 있다면 함께 확인
4. `BACKTEST_SPEC.md`가 있다면 함께 확인

문서와 실제 코드가 다르면 실제 코드를 우선합니다. 다만 코드 변경으로 설계 의도가 바뀌면 문서도 함께 갱신합니다.

## Core Direction

1. 구조는 `Modular Monolith + Lightweight Hexagonal + Selective DDD`를 기본으로 합니다.
2. 기존 CoinData처럼 멀티 서비스, Kafka 중심, 대형 Gradle 멀티 모듈 구조로 시작하지 않습니다.
3. 도메인 규칙이 있는 영역에는 DDD를 적용합니다.
4. 단순 CRUD 영역은 과하게 추상화하지 않습니다.
5. 외부 시장 데이터, 기존 CoinData 연동, CSV 업로드는 port/interface로 분리합니다.
6. 개인 거래소 API key, 실주문, 자동매매 기능은 MVP 범위에 넣지 않습니다.

## Recommended Stack

- Backend: Java 21, Spring Boot 3.x, Gradle
- Persistence: PostgreSQL, Spring Data JPA, QueryDSL if needed
- Cache: Redis only when there is a clear read/performance need
- Frontend: React + TypeScript or Next.js
- UI: Tailwind CSS + shadcn/ui style component set
- Local infra: Docker Compose

## Package And Module Rules

기본 패키지는 아래 형태를 권장합니다.

```text
com.example.tradejournal
```

기능 단위 패키지는 아래처럼 구성합니다.

```text
tradejournal
  auth
  user
  strategy
  backtest
  report
  share
  marketcontext
  common
```

각 기능 패키지는 필요에 따라 아래 계층을 둡니다.

```text
strategy
  presentation
  application
  domain
  infrastructure
```

계층 의존성은 아래 방향을 지킵니다.

```text
presentation -> application -> domain
infrastructure -> application/domain
```

Controller가 JPA repository를 직접 호출하지 않습니다. Application service가 use case를 담당하고, infrastructure는 DB, 외부 API, 파일, cache 같은 기술 세부사항을 담당합니다.

## Hexagonal And DDD Scope

DDD를 강하게 적용할 영역:

- Strategy
- StrategyRule
- BacktestRun
- BacktestEnvironment
- BacktestResult
- PerformanceMetric
- Report
- ReportSnapshot
- MarketContext

가볍게 처리할 영역:

- Comment
- Rating
- Bookmark
- ViewCount
- Tag
- Attachment

모든 단순 CRUD에 port를 만들지 않습니다. Port는 외부 시스템, 교체 가능한 데이터 공급원, 파일/CSV, 알림, AI 보조 기능처럼 경계가 명확한 곳에만 둡니다.

## Coding Rules

- 생성자 주입을 기본으로 합니다.
- Spring bean은 `@RequiredArgsConstructor` + `private final` 조합을 우선합니다.
- 불변 값 객체와 request/response DTO에는 Java `record`를 우선 고려합니다.
- JPA entity는 domain object와 분리합니다.
- Mapper를 명시적으로 둡니다.
- 비즈니스 규칙은 가능하면 domain 또는 application service에 둡니다.
- Controller에는 HTTP 변환, 인증 사용자 식별, request validation, response 변환만 둡니다.
- 예외는 사용자에게 노출할 메시지와 내부 원인을 분리합니다.
- 테스트하기 어려운 static utility를 남발하지 않습니다.
- 공통화는 실제 중복과 의미가 확인된 뒤 진행합니다.

권장 mapper 이름:

```text
DomainToEntity
EntityToDomain
DomainToResponse
RequestToCommand
CsvToMarketData
ExternalToDomain
```

## Report Safety Rules

리포트는 투자 의견 생성물이 아니라 백테스트 산출물입니다.

금지 표현:

- 매수 추천
- 매도 추천
- 목표가 제시
- 손절가 제시
- 향후 수익 보장
- 특정 자산 투자 권유
- 사용자 성향 기반 투자 조언

권장 표현:

- 백테스트상 진입 신호
- 백테스트상 청산 신호
- 테스트 기간 내 성과
- 설정된 수수료/슬리피지 기준 결과
- 과거 데이터 기준 산출값

리포트에는 가능한 한 아래 배지를 표시합니다.

```text
SAMPLE_DATA
REAL_DATA
CSV_UPLOADED
BACKTEST_ONLY
AI_ASSISTED_DRAFT
NOT_INVESTMENT_ADVICE
```

AI 기능을 넣더라도 투자 판단은 생성하지 않습니다. AI는 문장 다듬기, 정량 결과 요약, 목차 정리, 작성자 메모 보조 정도로 제한합니다.

## Data Policy

MVP에서는 개인 거래소 API key를 받지 않습니다.

시장 데이터 공급원은 아래 순서로 확장합니다.

1. `SampleMarketDataProvider`
2. `CsvMarketDataProvider`
3. `CoinDataApiMarketDataProvider`
4. 필요한 경우 read-only external provider

기존 CoinData DB에 직접 붙는 방식은 우선 피합니다. 기존 프로젝트와 연동해야 한다면 query API, read-only view, snapshot import, batch sync 중 하나를 선택합니다.

샘플 데이터는 완전 랜덤이 아니라 재현 가능한 seed 기반 시뮬레이션 데이터로 생성합니다. 상승장, 하락장, 횡보장, 급락 후 회복, 변동성 확대, 김프 확대 같은 구간을 포함합니다.

## Validation

백엔드 변경 후 기본 검증:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

프론트엔드 변경 후 기본 검증은 실제 package script 기준으로 실행합니다.

```powershell
npm run lint
npm run test
npm run build
```

프로젝트에 아직 해당 스크립트가 없다면 실행하지 않고, 어떤 검증을 생략했는지 작업 결과에 기록합니다.

## Commands Requiring Confirmation

사용자 명시 확인 없이 실행하지 않습니다.

- `git clean` 모든 형태. 먼저 `git clean -n`
- `git reset --hard`
- `git checkout -- <path>`
- `git stash drop`, `git stash clear`
- `git branch -D`
- `git push --force`, `git push --force-with-lease`
- `rm -rf`, PowerShell `Remove-Item -Recurse -Force`
- `.\gradlew.bat clean`
- 모든 `DROP`, `TRUNCATE`, `DELETE` SQL
- 실제 거래소 주문 API 호출
- 실제 사용자 자산 또는 계정에 영향을 줄 수 있는 외부 API 호출

10개 이상 파일에 영향을 주는 refactor는 먼저 `git status`를 확인하고, 변경 범위와 의도를 사용자에게 보고한 뒤 별도 브랜치에서 진행합니다.

