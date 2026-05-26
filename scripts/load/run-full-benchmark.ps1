#requires -Version 7.0
[CmdletBinding()]
param(
    [ValidateRange(1, [int]::MaxValue)]
    [int]$WarmupTargetTps = 5000,
    [ValidateRange(1, [int]::MaxValue)]
    [int]$WarmupDurationSec = 30,
    [ValidateRange(1, [int]::MaxValue)]
    [int]$MainTargetTps = 30000,
    [ValidateRange(1, [int]::MaxValue)]
    [int]$MainDurationSec = 120,
    [ValidateRange(1, [int]::MaxValue)]
    [int]$CaptureAtSec = 90,
    [ValidateRange(0, [int]::MaxValue)]
    [int]$CoolDownSec = 30,
    [string]$BootstrapServers = "localhost:9092",
    [string]$Topic = "ingestion-exchange.tick-raw",
    [string]$GrafanaBaseUrl = "http://localhost:3000",
    [string]$ResultDir,
    [switch]$SkipDockerUp
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$RepoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$RunStamp = Get-Date -Format "yyyyMMdd-HHmmss"
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $RepoRoot "docs\portfolio\screenshots\grafana\run-$RunStamp"
}

$ProduceScript = Join-Path $PSScriptRoot "produce-tick-raw.ps1"
$CaptureScript = Join-Path $PSScriptRoot "capture-grafana.ps1"
$StartRuntimeScript = Join-Path $RepoRoot "scripts\run\start-runtime.ps1"
$ComposeBase = Join-Path $RepoRoot "docker\docker-compose.yml"
$ComposeObservability = Join-Path $RepoRoot "docker\docker-compose.observability.yml"

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$FilePath failed with exit code $LASTEXITCODE"
    }
}

function Wait-HttpOk {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [ValidateRange(1, 600)][int]$TimeoutSec = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    do {
        try {
            $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
                return
            }
        }
        catch {
            Start-Sleep -Seconds 2
        }
    } while ((Get-Date) -lt $deadline)

    throw "Timed out waiting for $Url"
}

function Read-LastJsonObject {
    param([Parameter(Mandatory = $true)][string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return $null
    }

    $lines = @(Get-Content -LiteralPath $Path)
    [Array]::Reverse($lines)
    foreach ($line in $lines) {
        $trimmed = $line.Trim()
        if ($trimmed.StartsWith("{") -and $trimmed.EndsWith("}")) {
            return $trimmed | ConvertFrom-Json
        }
    }
    return $null
}

function Get-JavaVersionLines {
    try {
        return @((& java -version) 2>&1 | ForEach-Object { $_.ToString() })
    }
    catch {
        return @("java -version failed: $($_.Exception.Message)")
    }
}

function Get-HostMetadata {
    $cpuName = $null
    $cpuCores = $null
    $cpuLogical = $null
    $ramBytes = $null

    try {
        $cpu = Get-CimInstance -ClassName Win32_Processor | Select-Object -First 1
        $cpuName = $cpu.Name
        $cpuCores = $cpu.NumberOfCores
        $cpuLogical = $cpu.NumberOfLogicalProcessors
    }
    catch {
        $cpuName = "unavailable: $($_.Exception.Message)"
    }

    try {
        $computer = Get-CimInstance -ClassName Win32_ComputerSystem
        $ramBytes = [int64]$computer.TotalPhysicalMemory
    }
    catch {
        $ramBytes = $null
    }

    return [ordered]@{
        hostname = [System.Net.Dns]::GetHostName()
        os = $PSVersionTable.OS
        powerShellVersion = $PSVersionTable.PSVersion.ToString()
        cpu = [ordered]@{
            name = $cpuName
            cores = $cpuCores
            logicalProcessors = $cpuLogical
        }
        ramBytes = $ramBytes
        javaVersion = Get-JavaVersionLines
    }
}

function Invoke-ProduceToFiles {
    param(
        [Parameter(Mandatory = $true)][int]$TargetTps,
        [Parameter(Mandatory = $true)][int]$DurationSec,
        [Parameter(Mandatory = $true)][string]$StdoutPath,
        [Parameter(Mandatory = $true)][string]$StderrPath
    )

    & $ProduceScript `
        -TargetTps $TargetTps `
        -DurationSec $DurationSec `
        -BootstrapServers $BootstrapServers `
        -Topic $Topic `
        1> $StdoutPath `
        2> $StderrPath

    if ($LASTEXITCODE -ne 0) {
        throw "produce-tick-raw.ps1 failed with exit code $LASTEXITCODE. stderr: $StderrPath"
    }

    return Read-LastJsonObject -Path $StdoutPath
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$startedAt = Get-Date
$status = "running"
$warmupSummary = $null
$mainSummary = $null
$duringCaptures = @()
$afterCaptures = @()
$mainProcess = $null

try {
    if (-not $SkipDockerUp) {
        Invoke-Checked -FilePath "docker" -Arguments @("compose", "-f", $ComposeBase, "-f", $ComposeObservability, "up", "-d")
    }

    & $StartRuntimeScript -All
    if ($LASTEXITCODE -ne 0) {
        throw "start-runtime.ps1 failed with exit code $LASTEXITCODE"
    }

    Wait-HttpOk -Url "http://localhost:9090/-/healthy" -TimeoutSec 180
    Wait-HttpOk -Url "$GrafanaBaseUrl/api/health" -TimeoutSec 180

    $warmupStdout = Join-Path $ResultDir "warmup.stdout.jsonl"
    $warmupStderr = Join-Path $ResultDir "warmup.stderr.log"
    $warmupSummary = Invoke-ProduceToFiles -TargetTps $WarmupTargetTps -DurationSec $WarmupDurationSec -StdoutPath $warmupStdout -StderrPath $warmupStderr

    $mainStdout = Join-Path $ResultDir "main.stdout.jsonl"
    $mainStderr = Join-Path $ResultDir "main.stderr.log"
    $powerShellExe = (Get-Process -Id $PID).Path
    $mainArgs = @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-File", $ProduceScript,
        "-TargetTps", $MainTargetTps.ToString([System.Globalization.CultureInfo]::InvariantCulture),
        "-DurationSec", $MainDurationSec.ToString([System.Globalization.CultureInfo]::InvariantCulture),
        "-BootstrapServers", $BootstrapServers,
        "-Topic", $Topic
    )

    $mainProcess = Start-Process `
        -WindowStyle Hidden `
        -FilePath $powerShellExe `
        -ArgumentList $mainArgs `
        -RedirectStandardOutput $mainStdout `
        -RedirectStandardError $mainStderr `
        -WorkingDirectory $RepoRoot `
        -PassThru

    $captureDeadline = (Get-Date).AddSeconds($CaptureAtSec)
    while ((Get-Date) -lt $captureDeadline) {
        if ($mainProcess.HasExited) {
            throw "Main load process exited before capture point. exitCode=$($mainProcess.ExitCode)"
        }
        Start-Sleep -Seconds 1
    }

    $duringCaptures = @(& $CaptureScript -GrafanaBaseUrl $GrafanaBaseUrl -OutputDir $ResultDir -CaptureStamp (Get-Date -Format "yyyyMMdd-HHmm"))

    $mainProcess.WaitForExit()
    if ($mainProcess.ExitCode -ne 0) {
        throw "Main load failed with exit code $($mainProcess.ExitCode). stderr: $mainStderr"
    }
    $mainSummary = Read-LastJsonObject -Path $mainStdout

    if ($CoolDownSec -gt 0) {
        Start-Sleep -Seconds $CoolDownSec
    }
    $afterCaptures = @(& $CaptureScript -GrafanaBaseUrl $GrafanaBaseUrl -OutputDir $ResultDir -CaptureStamp (Get-Date -Format "yyyyMMdd-HHmm"))
    $status = "succeeded"
}
catch {
    $status = "failed"
    throw
}
finally {
    $endedAt = Get-Date
    $metadata = [ordered]@{
        status = $status
        startedAt = $startedAt.ToString("o")
        endedAt = $endedAt.ToString("o")
        durationSec = [Math]::Round(($endedAt - $startedAt).TotalSeconds, 3)
        bootstrapServers = $BootstrapServers
        topic = $Topic
        resultDir = $ResultDir
        scenario = [ordered]@{
            warmupTargetTps = $WarmupTargetTps
            warmupDurationSec = $WarmupDurationSec
            mainTargetTps = $MainTargetTps
            mainDurationSec = $MainDurationSec
            captureAtSec = $CaptureAtSec
            coolDownSec = $CoolDownSec
        }
        warmup = $warmupSummary
        main = $mainSummary
        captures = [ordered]@{
            duringLoad = @($duringCaptures)
            afterLoad = @($afterCaptures)
        }
        logs = [ordered]@{
            warmupStdout = Join-Path $ResultDir "warmup.stdout.jsonl"
            warmupStderr = Join-Path $ResultDir "warmup.stderr.log"
            mainStdout = Join-Path $ResultDir "main.stdout.jsonl"
            mainStderr = Join-Path $ResultDir "main.stderr.log"
        }
        environment = Get-HostMetadata
    }

    $metadataPath = Join-Path $ResultDir "run-metadata.json"
    $metadata | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $metadataPath -Encoding UTF8
    Write-Host "Run metadata: $metadataPath"
}
