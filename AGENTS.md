# Agent Instructions

이 저장소의 최신 프로젝트 기준 문서는 `CLAUDE.md`입니다. 작업을 시작할 때 먼저 `CLAUDE.md`를 읽고, 모듈 구조, 데이터 흐름, Kafka 토픽, Redis key, 기존 오타 패키지, 리스크 항목을 그 문서 기준으로 판단하세요.

## 우선순위

1. `CLAUDE.md`를 source of truth로 사용합니다.
2. 실제 코드가 `CLAUDE.md`와 다르면 실제 코드를 우선하고, 가능하면 문서도 함께 갱신합니다.
3. 기존 패키지 오타(`ingection`, `analystics`, `infre_exchange`, `infrastrcuture`, `infrastruct`, `clinet`, `parer`, `wirter`)는 별도 rename 요청이 없으면 그대로 맞춥니다.
4. 토픽명, Redis key, message record, entity/table 변경은 producer/consumer/query/API/test까지 함께 추적합니다.

## 기본 검증

문서 갱신 시점의 기본 검증 명령은 아래 두 개가 통과했습니다.

```powershell
.\gradlew.bat compileJava
.\gradlew.bat compileTestJava
```

기능 변경 시에는 변경 모듈 중심으로 targeted test를 추가 실행하세요. 전체 test는 Kafka, PostgreSQL, Redis, Testcontainers 환경 영향을 받을 수 있습니다.

## 금지/확인 필요 명령

사용자 명시 확인 없이 실행하지 마세요.

- `git clean` 모든 형태. 먼저 `git clean -n`.
- `git reset --hard`
- `git checkout -- <path>`
- `git stash drop`, `git stash clear`
- `git branch -D`
- `git push --force`, `git push --force-with-lease`
- `rm -rf`, PowerShell `Remove-Item -Recurse -Force`
- `.\gradlew.bat clean`
- 모든 `DROP`, `TRUNCATE`, `DELETE` SQL

10개 이상 파일에 영향을 주는 refactor는 `git status` 확인, 사용자 보고, 별도 브랜치 생성 후 진행합니다.
