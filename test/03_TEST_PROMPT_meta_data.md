# 03 — meta_data 테스트 프롬프트

> **선행 첨부 필수**: `00_TEST_COMMON_BASE.md`

---

## 작업 대상

`modules/meta_data/` 전체.

핵심 도메인:
- `Exchange` (거래소 마스터)
- `MarketCode` (마켓 코드 마스터)
- 매핑 정합성 (Exchange ↔ MarketCode)
- 자동완성 검색

이 모듈은 6개 중 **가장 단순**. CRUD + 캐시 + 매핑 무결성 점검이 전부.
대신 다른 모듈(market_data, analytics)이 의존하는 마스터 데이터 소스이므로 **신뢰성이 가장 중요**.

---

## 가장 중요한 테스트 대상

### 1. UseCase (단위 테스트)

| UseCase | 검증 핵심 |
|---|---|
| `GetExchangeListUseCase` | Repository 호출 + 결과 그대로 반환, 빈 리스트 케이스 |
| `GetMarketCodesByExchangeUseCase` | exchangeId로 필터링, 미존재 exchangeId 시 빈 리스트 |
| `SearchMarketCodeUseCase` | 검색어 매칭(대소문자/부분일치), exchange 필터, 빈 검색어 정책 |
| `CheckMappingIntegrityUseCase` | 거래소-마켓 매핑 누락/고아 레코드 검출 정확성 |

각 UseCase는 Port 전부 mock하여 격리 테스트.

### 2. `CheckMappingIntegrityUseCase` (특별 주목)

운영 점검용이므로 정확성이 매우 중요. 시나리오 매트릭스:

| 케이스 | Exchange | MarketCode | 기대 결과 |
|---|---|---|---|
| 정상 | A 존재 | A에 속한 MC 존재 | issue 0건 |
| Orphan MC | A 미존재 | A를 참조하는 MC 존재 | "orphan market code" 이슈 |
| Empty Exchange | A 존재 | A에 속한 MC 0건 | "exchange has no markets" 이슈 (정책에 따라 warn) |
| 중복 코드 | 같은 exchange 내 동일 `code` 2건 | | "duplicate code" 이슈 |
| baseAsset 누락 | MC에 baseAsset null | | "missing base asset" 이슈 |

각 케이스를 fixture 또는 테스트 빌더로 명확히 표현.

### 3. JPA Repository (`@DataJpaTest` + Testcontainers Postgres)

- `ExchangeRepository.findById`, `findAll`
- `MarketCodeRepository.findByExchangeId`
- `MarketCodeRepository.searchByQuery(String q, Long exchangeId)` 같은 커스텀 쿼리 — LIKE/ILIKE 정확성
- 인덱스 의존 쿼리는 explain 결과까지 검증할 필요 없음 (정확성만)

### 4. Entity Mapper (`EntityMapping<DOMAIN, ENTITY>`)

- Exchange / MarketCode 양방향 매핑
- enum 필드 (status 등) 라운드트립
- timestamp 필드 (createdAt, updatedAt) 처리

### 5. 캐시 (`Read*CacheAdapter` / `Write*CacheAdapter`)

- 마스터 데이터는 거의 변경되지 않으므로 long TTL 또는 무한 캐시
- 캐시 무효화 시점 (ex: 새 거래소 등록 시 list 캐시 무효화) 검증
- Redis 키: `RedisKeys.exchangeList()`, `RedisKeys.marketCodeByExchange(exchangeId)` 등

### 6. Kafka Publisher (마스터 변경 이벤트가 있다면)

- 토픽: `meta-data.exchange`, `meta-data.market-code`
- 마스터 데이터 변경 시 publish 되는지 (CRUD 서비스 통합 테스트)

### 7. Persistence Adapter (`@SpringBootTest` + Testcontainers)

- save → read 왕복 (동치)
- 트랜잭션 경계 (실패 시 롤백)

---

## 토픽 / Redis 키

| 종류 | 패턴 |
|---|---|
| Kafka 토픽 (publish) | `meta-data.exchange`, `meta-data.market-code` |
| Redis 키 | `ys:{env}:v1:meta:exchange:list` |
| Redis 키 | `ys:{env}:v1:meta:market-code:by-exchange:{exchangeId}` |
| Redis 키 | `ys:{env}:v1:meta:market-code:snapshot:{marketCodeId}` |

전부 `RedisKeys` 유틸 호출 결과로 검증.

---

## 테스트 빌더 패턴 권장

마스터 데이터는 필드가 많으므로 빌더로 가독성 확보.

```java
// src/test/java/.../fixtures/MetaDataFixtures.java
public final class MetaDataFixtures {
    private MetaDataFixtures() {}

    public static Exchange exchange(Long id, String code) {
        return Exchange.builder()
                .id(id)
                .code(code)
                .name(code + " Exchange")
                .quote("USD")
                .build();
    }

    public static MarketCode marketCode(Long id, Long exchangeId, String base) {
        return MarketCode.builder()
                .id(id)
                .exchangeId(exchangeId)
                .baseAsset(base)
                .code(base + "/USDT")
                .build();
    }
}
```

빌더가 없으면 record 생성자를 그대로 사용.

---

## 작업 절차 (이 모듈 한정)

1. `git ls-files modules/meta_data/src/main/java` 스캔
2. 분류: `application/usecase`, `domain`, `infrastructure/persistence`, `infrastructure/cache`, `infrastructure/messaging`
3. **순서**: UseCase 단위 → Mapper → Repository (`@DataJpaTest`) → Cache Adapter → Persistence Adapter → CheckMappingIntegrity 통합
4. `CheckMappingIntegrityUseCase` 는 다른 어떤 테스트보다 시나리오를 풍부하게 작성

---

## 검증 포인트 (이 모듈 한정)

- [ ] `CheckMappingIntegrityUseCase` 5종 시나리오 (정상/orphan/empty/duplicate/missing) 전부 커버
- [ ] `SearchMarketCodeUseCase` 의 대소문자/부분일치/exchange 필터 분기 모두 커버
- [ ] JPA Repository `@DataJpaTest` + Testcontainers Postgres
- [ ] 캐시 무효화 시점 (마스터 변경 → 캐시 invalidate) 검증
- [ ] Redis 키 `RedisKeys` 유틸 사용 검증
- [ ] Kafka publish (있을 경우) 토픽명 검증
- [ ] 테스트 빌더 또는 fixture 모듈 정리 (`MetaDataFixtures`)
