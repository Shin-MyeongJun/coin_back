# GC pause 측정 가이드 — DSL-JSON vs Jackson (binance_ingestion)

> ⚠️ 본 문서는 `experiment/gc-pause-measurement-2026-05-26` 브랜치에서만 유효합니다.
> 메인 브랜치에는 머지하지 않으며, 측정 종료 후 본 브랜치는 폐기합니다.

## 1. 목적

다음 가설을 직접 측정으로 검증합니다.

> "DSL-JSON 도입의 운영 안정성 효과는 throughput 개선이 아니라
>  alloc 패턴 변화로 인한 GC pause 단축에서 비롯됐다."

`binance_ingestion`은 5,000~8,000 msg/sec, 피크 60,000 msg/sec 부하라 GC 차이를 관찰하기에 충분합니다.
`upbit_ingestion`(400~600 msg/sec)은 부하가 낮아 비교 의미가 적어 본 측정 범위에서 제외합니다.

## 2. 사전 조건 (두 측정 모두 동일해야 함)

| 항목 | 값 |
| --- | --- |
| Heap size | `-Xms2g -Xmx2g` 고정 (스크립트에 박혀 있음) |
| GC 알고리즘 | G1GC 고정 (스크립트에 박혀 있음) |
| 시장 시간대 | 두 측정 모두 같은 요일/시간대, 가능하면 연속 1시간 내 |
| 구독 마켓 코드 | `application.yml` 변경 금지 — 동일 구독 유지 |
| Kafka 상태 | 두 측정 사이에 토픽/lag 상태가 같아야 함 |

이 4가지가 어긋나면 비교가 무의미합니다.

## 3. 사전 점검 체크리스트

- [ ] `git branch` → 현재 브랜치가 `experiment/gc-pause-measurement-2026-05-26`
- [ ] `.\gradlew.bat :binance_ingestion:compileJava` 성공
- [ ] `.\gradlew.bat :ingestion_exchange_shard:test --tests TickRawHandlerTest` 통과
- [ ] `.\gradlew.bat :binance_ingestion:test --tests BinanceParserParityTest` 통과 (어댑터 동치성)
- [ ] Kafka 기동: `docker compose up -d kafka` (이미 떠 있으면 skip)
- [ ] 직전 측정 잔여 프로세스 없음 (`Get-Process -Name java`)

## 4. 실행 순서

### 4.1 DSL-JSON 측정 (30분)

```powershell
.\scripts\measure\run-binance-with-gc-log.ps1 -Mode dsljson -DurationMinutes 30
```

- 시작 직후 1분간 wake-up 트래픽이 평탄해질 때까지 wait.
- `logs/gc-binance-dsljson-<ts>.log` 가 매초 갱신되는지 확인.
- 종료 시 콘솔에 `Total received messages: <N>` 출력.

### 4.2 인터벌 — JVM/Kafka 안정화 대기

직전 측정 종료 후 1~2분 대기. 같은 프로세스가 죽었는지 확인.

```powershell
Get-Process -Name java -ErrorAction SilentlyContinue
```

### 4.3 Jackson 측정 (30분, 같은 조건)

```powershell
.\scripts\measure\run-binance-with-gc-log.ps1 -Mode jackson -DurationMinutes 30
```

### 4.4 분석

```powershell
.\scripts\measure\compare-gc.ps1 `
    -DslJsonLog logs\gc-binance-dsljson-*.log `
    -JacksonLog logs\gc-binance-jackson-*.log
```

스크립트는 두 로그를 각각 분석한 뒤, `logs\measurement-summary.csv`를 읽어 부하 동등성을 점검합니다 (`Ratio`가 0.8~1.25 범위 밖이면 경고).

## 5. 결과 해석 매트릭스

| DSL-JSON GC pause | Jackson GC pause | 해석 |
| --- | --- | --- |
| Jackson 대비 **현저히 짧음** (avg, p99, sum 모두) | — | 가설 입증. DSL-JSON의 alloc 절감이 GC pause 단축으로 직결됨 |
| 비슷함 | 비슷함 | DSL-JSON 효과는 alloc 절감 자체에 그치고 GC pause는 다른 요인이 지배. throughput 개선이 본질 |
| DSL-JSON pause가 더 김 (드뭄) | — | 부하 차이, JVM 워밍업, 캐시 효과 등 측정 노이즈 의심. 4.1~4.3 재현 필요 |

부하 동등성(Ratio)이 0.8~1.25 범위 밖이면 어떤 결과든 비교가 무효입니다.

## 6. 결과 기록 양식

`docs/portfolio/gc-measurement-result.md`에 다음 항목으로 정리합니다.

```markdown
# GC pause measurement result — binance_ingestion

- 측정 일자: YYYY-MM-DD HH:MM (KST)
- Heap: 2g (Xms=Xmx)
- GC: G1GC
- 측정 길이: 30 min × 2
- 부하 (received msg 합계):
  - DSL-JSON: <N>
  - Jackson:  <N>
  - 비율:     <ratio>
- GC pause 요약:
  - DSL-JSON: avg=<>, p95=<>, p99=<>, sum=<>
  - Jackson:  avg=<>, p95=<>, p99=<>, sum=<>
- 결론: <가설 입증 여부 1~3줄>
- 원본 로그: logs/gc-binance-dsljson-<ts>.log, logs/gc-binance-jackson-<ts>.log
```

결과 문서는 별도 커밋으로 분리하여 메인 브랜치 cherry-pick이 가능하게 둡니다.

## 7. 브랜치 폐기 절차

측정과 결과 기록이 끝났다면:

```powershell
# 결과 문서 / 로그만 메인에 옮길 경우 (필요 시)
git checkout master
git checkout experiment/gc-pause-measurement-2026-05-26 -- docs/portfolio/gc-measurement-result.md
git checkout experiment/gc-pause-measurement-2026-05-26 -- logs/gc-binance-*.log
git add docs/portfolio/gc-measurement-result.md
git commit -m "docs: GC pause measurement result"

# 측정 브랜치 자체 폐기
git branch -D experiment/gc-pause-measurement-2026-05-26
# push 했다면 origin도 정리
# git push origin --delete experiment/gc-pause-measurement-2026-05-26
```

기존에 stash해둔 작업이 있다면 복구:

```powershell
git stash list
git stash pop   # 또는 git stash apply stash@{0}
```

## 8. FAQ / 주의

- **왜 `web-application-type: none` 인 서비스에 actuator가 뜨나?**
  `management.server.port: 9092` 설정으로 별도 management 서버가 기동합니다. main web 서버 없음.
- **measurement-summary.csv가 BOM(UTF-16)로 작성되어 깨질 수 있다?**
  PS1 스크립트에서 명시적으로 `-Encoding UTF8`로 append합니다.
- **GC 로그가 비어 있다.**
  `JAVA_TOOL_OPTIONS`가 gradle daemon에 전달되지 않으면 발생. 스크립트가 `JAVA_TOOL_OPTIONS`를 set한 직후 bootRun을 실행하므로 보통 동작합니다. 안 되면 `.\gradlew.bat --no-daemon` 사용을 검토.
- **30분이 너무 짧다 / 길다.**
  GC가 충분히 반복돼야 통계가 의미 있으므로 최소 15분, 권장 30분. 부하가 평탄한 구간을 우선.
