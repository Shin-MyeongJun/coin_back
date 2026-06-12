param(
    [Parameter(Mandatory)][string]$DslJsonLog,
    [Parameter(Mandatory)][string]$JacksonLog
)

# Resolve relative globs (e.g. logs\gc-binance-dsljson-*.log) to latest match.
function Resolve-LatestLog([string]$pattern) {
    $matches = @(Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)
    if ($matches.Count -eq 0) {
        Write-Host "No log matched pattern: $pattern" -ForegroundColor Red
        return $null
    }
    return $matches[0].FullName
}

$dslPath = Resolve-LatestLog $DslJsonLog
$jacksonPath = Resolve-LatestLog $JacksonLog

if (-not $dslPath -or -not $jacksonPath) { return }

. "$PSScriptRoot\analyze-gc.ps1" -LogPath $dslPath -Label "DSL-JSON"
. "$PSScriptRoot\analyze-gc.ps1" -LogPath $jacksonPath -Label "Jackson"

Write-Host ""
Write-Host "=== Load parity check ===" -ForegroundColor Cyan

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$summaryPath = Join-Path $projectRoot "logs\measurement-summary.csv"
if (-not (Test-Path $summaryPath)) {
    Write-Host "measurement-summary.csv not found; skipping parity check." -ForegroundColor Yellow
    return
}

$summary = Import-Csv $summaryPath -Header Mode,Count,LogFile,DurationMin
$dslLeaf = Split-Path $dslPath -Leaf
$jacksonLeaf = Split-Path $jacksonPath -Leaf
$dslRow = $summary | Where-Object { $_.LogFile -like "*$dslLeaf*" } | Select-Object -Last 1
$jackRow = $summary | Where-Object { $_.LogFile -like "*$jacksonLeaf*" } | Select-Object -Last 1

if ($dslRow -and $jackRow) {
    Write-Host "DSL-JSON received: $($dslRow.Count) messages"
    Write-Host "Jackson  received: $($jackRow.Count) messages"
    $ratio = [double]$dslRow.Count / [double]$jackRow.Count
    Write-Host ("Ratio: {0:F2} (close to 1.0 means valid comparison)" -f $ratio)
    if ($ratio -lt 0.8 -or $ratio -gt 1.25) {
        Write-Host "WARNING: load differs by more than 20% — comparison reliability is low" -ForegroundColor Red
    } else {
        Write-Host "OK: load parity within tolerance" -ForegroundColor Green
    }
} else {
    Write-Host "Could not find both rows in measurement-summary.csv" -ForegroundColor Yellow
}
