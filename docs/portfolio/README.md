# Portfolio Artifacts

This directory contains generated and supporting artifacts for portfolio quantitative evidence.

| File | Role |
| --- | --- |
| `test-stats.md` | Portfolio-ready Markdown table with module test counts, pass/fail counts, line coverage, and runtime. |
| `test-stats.json` | Machine-readable version of the same data for charts or slide automation. |
| `test-stats-howto.md` | Command, Docker prerequisite, and output semantics for regenerating the stats. |

Regenerate the stats with:

```powershell
.\gradlew.bat portfolioStats
```

Use the Docker-free partial mode when Testcontainers infrastructure is unavailable:

```powershell
.\gradlew.bat portfolioStats -PskipIntegration=true
```

