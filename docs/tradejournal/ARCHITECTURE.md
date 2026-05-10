# TradeJournal Architecture

## Overview

TradeJournal은 전략 저장, 백테스트 실행 조건 관리, 백테스트 결과 리포트화, 공유/평점/필터링을 제공하는 투자 리서치 서비스입니다.

이 프로젝트의 목표는 실거래 자동화가 아니라, 백엔드 CRUD 역량과 사용자 중심 서비스 완성도를 보여주는 것입니다. 따라서 기존 CoinData 프로젝트와 같은 실시간 데이터 파이프라인 구조가 아니라, 모듈형 모놀리식 구조로 시작합니다.

## Architecture Decision

선택한 구조:

```text
Modular Monolith + Lightweight Hexagonal + Selective DDD
```

선택 이유:

- CRUD 서비스는 단일 애플리케이션으로 배포하는 편이 개발과 운영이 단순합니다.
- 인증, 권한, 전략, 리포트, 공유 기능은 하나의 트랜잭션 경계 안에서 처리할 일이 많습니다.
- 전략/백테스트/리포트는 도메인 규칙이 있으므로 DDD 적용 가치가 있습니다.
- 댓글, 평점, 북마크, 조회수 같은 기능은 단순 CRUD에 가깝기 때문에 과한 추상화를 피합니다.
- 기존 CoinData 연동은 직접 의존이 아니라 port/interface로 열어두는 편이 안전합니다.

## Non Goals

MVP에서 하지 않습니다.

- 실제 거래소 주문
- 개인 거래소 API key 저장
- 자동매매
- AI 투자 추천
- 미래 수익 예측
- 기존 CoinData DB 직접 결합
- Kafka 기반 실시간 파이프라인 재구현
- 처음부터 대형 Gradle 멀티 모듈 구조 적용

## Modules

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

### auth

로그인, 토큰, 인증 사용자 식별, 권한 처리를 담당합니다.

주요 기능:

- 회원가입
- 로그인
- 토큰 발급/갱신
- 인증 사용자 조회
- 역할 기반 접근 제어

### user

사용자 프로필과 기본 설정을 담당합니다.

주요 기능:

- 프로필 조회/수정
- 닉네임
- 공개 프로필
- 기본 백테스트 설정

### strategy

전략 정의와 전략 규칙을 담당합니다.

주요 기능:

- 전략 생성/수정/삭제
- 전략 공개/비공개
- 전략 유형 관리
- 전략 파라미터 관리
- 태그 관리

초기 전략 유형:

- MovingAverageCrossStrategy
- KimchiPremiumStrategy

### backtest

백테스트 실행 조건, 실행 결과, 성과 지표를 담당합니다.

주요 기능:

- 백테스트 조건 저장
- 백테스트 실행 요청
- 결과 저장
- 수익률, MDD, 승률, 거래 횟수 계산
- 벤치마크 비교

### report

백테스트 결과를 구조화된 리포트로 저장하고 조회합니다.

주요 기능:

- 리포트 생성
- 리포트 상세 조회
- 리포트 공개/비공개
- 리포트 스냅샷 저장
- 차트 데이터 제공
- 작성자 메모 관리

### share

공유, 조회수, 평점, 댓글, 북마크를 담당합니다.

주요 기능:

- 공개 리포트 탐색
- 공유 링크
- 조회수 증가
- 댓글
- 평점
- 북마크
- 랭킹/정렬

### marketcontext

백테스트 기간의 시장 맥락 데이터를 담당합니다.

주요 기능:

- 경제 이벤트 조회
- 뉴스 이벤트 참조
- 시장 데이터 공급원 추상화
- CSV 업로드 데이터 처리
- 기존 CoinData query API 연동 adapter

### common

공통 예외, 공통 response, 공통 time/date type, 공통 validation utility를 둡니다.

`common`에는 특정 도메인 규칙을 넣지 않습니다.

## Package Layout

각 기능 모듈은 기본적으로 아래 구조를 사용합니다.

```text
com.example.tradejournal.strategy
  presentation
    StrategyController
    StrategyRequest
    StrategyResponse
  application
    StrategyCommandService
    StrategyQueryService
    command
    result
    port
      out
  domain
    Strategy
    StrategyRule
    StrategyStatus
    StrategyType
    StrategyRepository
  infrastructure
    persistence
      StrategyJpaEntity
      StrategyJpaRepository
      StrategyPersistenceAdapter
      StrategyMapper
```

외부 데이터 연동이 필요한 경우:

```text
com.example.tradejournal.marketcontext
  application
    MarketDataQueryService
    port
      out
        MarketDataProvider
        EconomicEventProvider
        NewsEventProvider
  domain
    MarketCandle
    MarketDataSource
    EconomicEvent
    NewsEvent
  infrastructure
    sample
      SampleMarketDataProvider
    csv
      CsvMarketDataProvider
    coindata
      CoinDataApiMarketDataProvider
```

## Dependency Rules

허용 방향:

```text
presentation -> application -> domain
infrastructure -> application/domain
```

금지:

- Controller에서 JPA repository 직접 호출
- domain에서 Spring, JPA, HTTP client 의존
- feature module이 다른 feature의 infrastructure 직접 호출
- common에 도메인 규칙 넣기
- 단순 CRUD마다 불필요한 port/interface 만들기

Cross-module 호출은 가능한 application service 또는 facade를 통해 수행합니다.

## Domain Model

핵심 도메인:

```text
Strategy
StrategyRule
BacktestEnvironment
BacktestRun
BacktestResult
PerformanceMetric
Report
ReportSnapshot
MarketContext
```

보조 도메인:

```text
Comment
Rating
Bookmark
ViewCount
Tag
```

## Report Structure

리포트는 템플릿 기반으로 생성합니다.

```text
1. 전략 개요
2. 백테스트 환경
3. 진입/청산 조건
4. 성과 지표
5. 수익률 차트
6. MDD 차트
7. 거래 내역
8. 주요 변동 구간
9. 관련 경제 이벤트/뉴스
10. 리스크와 한계
11. 작성자 메모
```

시스템이 자동으로 채우는 데이터:

- 테스트 기간
- 데이터 출처
- 종목/거래소
- 봉 간격
- 초기 자본
- 수수료
- 슬리피지
- 총수익률
- MDD
- 승률
- 거래 횟수
- 평균 손익
- 진입/청산 신호 발생 시점
- 경제 이벤트/뉴스 이벤트 목록

사용자가 작성하는 데이터:

- 전략 설명
- 해석 메모
- 개선 아이디어
- 리스크 메모

## Report Safety

리포트는 투자 권유가 아니라 백테스트 결과 요약입니다.

권장 용어:

- 백테스트상 진입 신호
- 백테스트상 청산 신호
- 과거 데이터 기준 결과
- 설정된 수수료/슬리피지 기준 결과

피해야 할 용어:

- 매수 추천
- 매도 추천
- 목표가
- 손절가 추천
- 향후 수익 예상
- 지금 진입

리포트 배지:

```text
SAMPLE_DATA
REAL_DATA
CSV_UPLOADED
BACKTEST_ONLY
AI_ASSISTED_DRAFT
NOT_INVESTMENT_ADVICE
```

## Data Source Strategy

시장 데이터 공급원은 interface로 분리합니다.

```java
public interface MarketDataProvider {
    List<MarketCandle> getCandles(MarketDataQuery query);
}
```

구현 우선순위:

1. `SampleMarketDataProvider`
2. `CsvMarketDataProvider`
3. `CoinDataApiMarketDataProvider`

초기 MVP는 샘플 데이터와 CSV 업로드까지만 목표로 합니다. 기존 CoinData 프로젝트 연동은 고도화 단계에서 query API adapter로 추가합니다.

## Sample Data

샘플 데이터는 `local` 또는 `dev` profile에서만 생성합니다.

원칙:

- seed를 고정해 재현 가능하게 만듭니다.
- 완전 랜덤 데이터를 피합니다.
- 상승장, 하락장, 횡보장, 급락 후 회복, 변동성 확대 구간을 포함합니다.
- 성공한 전략뿐 아니라 손실 전략, 낮은 평점, 댓글 없는 리포트, 비공개 리포트도 포함합니다.
- 백테스트 결과와 리포트 수치가 서로 모순되지 않게 도메인 로직을 통해 생성합니다.

## Filtering And Sorting

공개 리포트 탐색 필터:

- 기간
- 거래소
- 종목
- 봉 간격
- 전략 유형
- 데이터 출처
- 수수료/슬리피지 조건
- 총수익률
- MDD
- 승률
- 거래 횟수
- 조회수
- 평점
- 북마크 수
- 최신순

## API Groups

예상 API 그룹:

```text
/api/auth
/api/users
/api/strategies
/api/backtests
/api/reports
/api/public/reports
/api/reports/{reportId}/comments
/api/reports/{reportId}/ratings
/api/reports/{reportId}/bookmarks
/api/market-context
```

## Persistence Sketch

초기 테이블 후보:

```text
users
strategies
strategy_rules
backtest_runs
backtest_results
backtest_trades
reports
report_snapshots
report_comments
report_ratings
report_bookmarks
report_view_logs
tags
strategy_tags
market_events
```

리포트는 생성 시점의 조건과 결과를 snapshot으로 보관합니다. 이후 전략이 수정되어도 과거 리포트의 조건과 결과가 바뀌지 않아야 합니다.

## Testing Strategy

우선순위:

1. domain 단위 테스트
2. application service 테스트
3. repository adapter 테스트
4. controller slice 테스트
5. 주요 사용자 흐름 통합 테스트

중요 테스트:

- 전략 생성/수정 권한
- 백테스트 환경 validation
- 수수료/슬리피지 반영
- 성과 지표 계산
- 리포트 snapshot 불변성
- 공개/비공개 리포트 접근 제어
- 평점 중복 방지
- 북마크 토글
- 필터/정렬 조건

## Roadmap

### MVP

- 회원가입/로그인
- 전략 CRUD
- 백테스트 조건 저장
- 샘플 데이터 기반 백테스트
- 리포트 생성/조회
- 공개 리포트 목록
- 기본 필터/정렬
- Docker Compose

### Portfolio Version

- 댓글/평점/북마크
- 리포트 차트
- CSV 업로드
- 고급 필터
- 테스트 코드 보강
- API 문서
- 프론트엔드 완성
- 배포 문서

### Advanced

- 기존 CoinData query API 연동
- 경제 이벤트/뉴스 이벤트 자동 참조
- 전략 비교
- 리포트 랭킹
- 관리자 기능
- AI 문장 보조 기능

