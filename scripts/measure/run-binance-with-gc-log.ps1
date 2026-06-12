param(
    [Parameter(Mandatory)][ValidateSet('dsljson','jackson')]
    [string]$Mode,
    [int]$DurationMinutes = 30
)

# === Resolve JAVA_HOME ===
if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
    if (-not $env:JAVA_HOME) {
        $env:JAVA_HOME = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
    }
}
if (-not $env:JAVA_HOME) {
    Write-Host "ERROR: JAVA_HOME not found." -ForegroundColor Red
    Write-Host '  Set it first: $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"'
    exit 1
}
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# === Cleanup JAVA_TOOL_OPTIONS (prevent stale Xlog injection) ===
if ($env:JAVA_TOOL_OPTIONS) {
    Write-Host "JAVA_TOOL_OPTIONS detected in session, clearing." -ForegroundColor Yellow
    Write-Host "  Old value: $env:JAVA_TOOL_OPTIONS" -ForegroundColor DarkGray
    Remove-Item env:JAVA_TOOL_OPTIONS
}

$persistentTool = [Environment]::GetEnvironmentVariable("JAVA_TOOL_OPTIONS", "User")
if ($persistentTool) {
    Write-Host "WARN: User-scope JAVA_TOOL_OPTIONS is set:" -ForegroundColor Red
    Write-Host "  $persistentTool"
    Write-Host "  Remove with: [Environment]::SetEnvironmentVariable('JAVA_TOOL_OPTIONS', `$null, 'User')"
    Read-Host "Press Enter to continue, Ctrl+C to abort"
}

# === Paths ===
$timestamp = Get-Date -Format "yyyyMMdd-HHmm"
$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$logDir = Join-Path $projectRoot "logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$gcLogAbsolute = Join-Path $logDir "gc-binance-$Mode-$timestamp.log"

# Escape colon for -Xlog parser (Windows path issue)
# C:\Users\... -> C\:/Users/...
$gcLogForJvm = $gcLogAbsolute.Replace('\','/').Replace(':','\:')

# === JVM args ===
$jvmArgsString = "-Xlog:gc*:file=$gcLogForJvm" +
                 ":time,uptime,level,tags" +
                 ":filecount=10,filesize=20M " +
                 "-XX:+UseG1GC " +
                 "-Xms2g -Xmx2g"

$profiles = if ($Mode -eq 'jackson') { 'local,jackson-test' } else { 'local' }

Write-Host "=== Starting binance_ingestion ===" -ForegroundColor Cyan
Write-Host "Mode:         $Mode"
Write-Host "Duration:     $DurationMinutes minutes"
Write-Host "GC log (rel): $gcLogRelative"
Write-Host "GC log (abs): $gcLogAbsolute"
Write-Host "Profile:      $profiles"
Write-Host "JVM args:     $jvmArgsString"
Write-Host ""

Push-Location $projectRoot
try {
    $job = Start-Job -ScriptBlock {
        param($root, $jvmArgs, $profiles, $javaHome)
        $env:JAVA_HOME = $javaHome
        $env:PATH = "$javaHome\bin;$env:PATH"
        Remove-Item env:JAVA_TOOL_OPTIONS -ErrorAction SilentlyContinue
        Set-Location $root

        & ".\gradlew.bat" ":binance_ingestion:bootRun" `
            "-PjvmArgs=$jvmArgs" `
            "--args=--spring.profiles.active=$profiles" `
            "-x" "processAot" `
            "-x" "processTestAot" 2>&1
    } -ArgumentList $projectRoot, $jvmArgsString, $profiles, $env:JAVA_HOME

    Write-Host "Job ID: $($job.Id), measurement started. Auto-stop in $DurationMinutes min." -ForegroundColor Yellow

    # Show early log to catch boot failures
    Start-Sleep -Seconds 15
    $earlyLog = Receive-Job -Job $job -Keep
    if ($earlyLog) {
        Write-Host "--- Early output (first 15s) ---" -ForegroundColor DarkGray
        $earlyLog | Select-Object -Last 30 | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }
        Write-Host "---" -ForegroundColor DarkGray
    }

    Start-Sleep -Seconds (($DurationMinutes * 60) - 15)

    # Capture counter
    try {
        $metric = Invoke-RestMethod "http://localhost:9092/actuator/metrics/ingestion.tick.raw.received" -TimeoutSec 5
        $count = ($metric.measurements | Where-Object { $_.statistic -eq 'COUNT' }).value
        Write-Host "Measurement done. Received total: $count messages" -ForegroundColor Green
        "$Mode,$count,$gcLogAbsolute,$DurationMinutes" |
            Out-File -FilePath (Join-Path $logDir "measurement-summary.csv") -Append -Encoding UTF8
    } catch {
        Write-Host "Counter query failed (Spring may not have started): $_" -ForegroundColor Red
    }

    Stop-Job -Job $job
    Remove-Job -Job $job -Force
} finally {
    Pop-Location
}

# === Verify ===
if (Test-Path $gcLogAbsolute) {
    $size = (Get-Item $gcLogAbsolute).Length
    $lineCount = (Get-Content $gcLogAbsolute | Measure-Object -Line).Lines
    $sizeKb = [Math]::Round($size / 1KB, 1)
    Write-Host ""
    Write-Host "[OK] GC log: $gcLogAbsolute ($sizeKb KB, $lineCount lines)" -ForegroundColor Green
    if ($lineCount -lt 5) {
        Write-Host "  WARN: too few lines - JVM args may not have applied, or no GC occurred" -ForegroundColor Yellow
    } else {
        Write-Host "  First 3 lines:" -ForegroundColor DarkGray
        Get-Content $gcLogAbsolute -TotalCount 3 | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }
    }
} else {
    Write-Host "[FAIL] GC log not created: $gcLogAbsolute" -ForegroundColor Red
    Write-Host "  bootRun likely did not pick up jvmArgs - verify build.gradle"
}