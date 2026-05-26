# Portfolio Test Stats How-to

## Run

```powershell
.\gradlew.bat portfolioStats
```

The task runs each subproject `test`, runs `jacocoTestReport` after tests, aggregates Surefire XML and Jacoco XML, then rewrites:

- `docs/portfolio/test-stats.md`
- `docs/portfolio/test-stats.json`

It also prints a CI-friendly summary:

```text
Total: 480 tests, 464 passed, 13 failed, line coverage 25.1%
```

## Prerequisites

Many integration tests rely on Testcontainers, so Docker should be running for the full command.

For a Docker-free partial run:

```powershell
.\gradlew.bat portfolioStats -PskipIntegration=true
```

With `-PskipIntegration=true`, the Gradle setup attempts to exclude JUnit tags `docker`, `integration`, and `it`, and class patterns `*IT` and `*IntegrationTest`. Tests that are not tagged or named this way may still run. Any remaining test failures are captured in the report instead of stopping `portfolioStats`.

## Output Semantics

- `Module` follows `settings.gradle` order and uses the Gradle project path as-is.
- `Failed` is `failures + errors`.
- `Passed` is `tests - failed - skipped`.
- `Coverage (line)` is `covered / (covered + missed)` from each module's Jacoco XML.
- Modules with no tests or no Jacoco XML are still listed with `0` tests and `N/A` coverage.
- The Markdown table highlights failed counts in red HTML so the row can be dropped into portfolio slide 9.
