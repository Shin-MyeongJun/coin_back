# crawling_ingestion Design

## 수집 데이터 결정

| 심볼 | 이름 | 소스 | 김프 시스템 연관성 |
|------|------|------|-------------------|
| SP500 | S&P 500 | Yahoo Finance (`^GSPC`) | 글로벌 위험선호 — BTC 방향성 동조 |
| NASDAQ | NASDAQ Composite | Yahoo Finance (`^IXIC`) | 기술주 심리, BTC 상관도 높음 |
| VIX | CBOE Volatility Index | Yahoo Finance (`^VIX`) | 공포지수 — 김프 급등/급락 예측 |
| DXY | US Dollar Index | Yahoo Finance (`DX-Y.NYB`) | 달러 강세 → 원화 약세 → 김프 압력 |
| GOLD | Gold Futures | Yahoo Finance (`GC=F`) | 안전자산 수요, 크립토 대체재 |
| OIL | Crude Oil WTI | Yahoo Finance (`CL=F`) | 위험자산 심리 지표 |
| KOSPI200 | KOSPI 200 Index | Investing.com | 국내 기관 포지션 — 김프 직결 |
| US10Y | US 10-Year Treasury Yield | Investing.com | 거시 금리 환경, 리스크 프리미엄 |

fx_ingestion이 KRW/USD 환율 수집 중 → 환율 제외.

## 크롤링 전략

- **Yahoo Finance**: `GET /v7/finance/quote?symbols=...` JSON API, 6개 심볼 배치 쿼리. 폴링 2초.
  - `regularMarketTime`은 Unix seconds → ×1000 변환 필요
  - `Retry.backoff(3, 500ms)` 재시도
- **Investing.com**: Jsoup HTML 스크래핑. CSS 셀렉터 우선순위:
  1. `[data-test="instrument-price-last"]` (신규 포맷)
  2. `#last_last` (구 포맷)
  - Cloudflare 보호로 실패 가능 → 빈 리스트 반환, Yahoo 단독 운영으로 graceful degrade
  - 폴링 5초

## Kafka 토픽

- `crawling-ingestion.global-index` (partition=1, replica=1)

## 메시지 스키마

`MacroIndicatorMessage(symbol, price: BigDecimal, timestamp: long, source: "yahoo"|"investing")` — contracts 기존 record 재사용 (신규 파일 불필요).

## 상속/활용

- `economic_ind_ingestion_shard`: 의존성 포함, 클래스 상속 없음 (배치 스케줄 패턴 미사용 — 실시간 폴링 전용)
- `infra_shard.RawToDomain<RAW, DOMAIN>`: Yahoo 매퍼(`YahooQuoteMapper`)에 활용
