<#
.SYNOPSIS
Runs Kafka-to-API SSE smoke tests for CoinData.

.DESCRIPTION
This script opens one API SSE endpoint, produces one JSON message to the matching Kafka topic, and verifies that the expected text is captured from the SSE stream.
It is intended for local Docker-based validation after Kafka and the API module are running.

.EXAMPLE
.\scripts\e2e\stream-smoke.ps1 -Scenario all -TimeoutSeconds 5

Runs every supported stream scenario.

.EXAMPLE
.\scripts\e2e\stream-smoke.ps1 -Scenario tick-raw -ApiBaseUrl http://localhost:8080

Runs only the market-data tick SSE scenario.

.EXAMPLE
.\scripts\e2e\stream-smoke.ps1 -ListScenarios

Prints the available scenario names.
#>
[CmdletBinding()]
param(
    [ValidateSet("all", "tick-raw", "premium-raw", "premium-detail-raw", "tick-candle", "premium-candle", "premium-detail-candle", "tick-indicator", "premium-indicator")]
    [string]$Scenario = "all",

    [string]$ApiBaseUrl = "http://localhost:8080",

    [string]$KafkaContainer = "kafka",

    [string]$KafkaBootstrap = "localhost:9092",

    [int]$TimeoutSeconds = 20,

    [switch]$SkipHealthCheck,

    [switch]$ListScenarios
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Scenarios = [ordered]@{
    "tick-raw" = @{
        Endpoint = "/api/v1/stream/ticks?marketCodeId=9001"
        Topic    = "market-data.tick"
        Payload  = '{"marketCodeId":9001,"bid":100.00,"ask":101.00,"timestamp":1710000000000}'
        Expected = @('"marketCodeId":9001', '"bid":"100.00"', '"timestamp":1710000000000')
    }
    "premium-raw" = @{
        Endpoint = "/api/v1/stream/premium"
        Topic    = "market-data.premium"
        Payload  = '{"symbol":"E2E-PREMIUM","baseExchangeId":1,"compareExchangeId":2,"bid":1.23,"ask":1.45,"timestamp":1710000000000}'
        Expected = @('"symbol":"E2E-PREMIUM"', '"bid":"1.23"', '"timestamp":1710000000000')
    }
    "premium-detail-raw" = @{
        Endpoint = "/api/v1/stream/premium-detail/raw"
        Topic    = "market-data.premium-detail"
        Payload  = '{"symbol":"E2E-PD-RAW","baseExchangeId":1,"compareExchangeId":2,"baseBid":100.00,"baseAsk":101.00,"baseQuoteVal":1000.00,"compareBid":99.00,"compareAsk":100.00,"compareQuoteVal":990.00,"timestamp":1710000000000}'
        Expected = @('"symbol":"E2E-PD-RAW"', '"baseExchangeId":1', '"timestamp":1710000000000')
    }
    "tick-candle" = @{
        Endpoint = "/api/v1/stream/candles/close?type=tick"
        Topic    = "analytics.tick-candle"
        Payload  = '{"marketCodeId":"9001","interval":"1m","open":"100.00","high":"106.00","low":"98.00","close":"105.00","bucketOpenTs":"1710000000000","bucketCloseTs":"1710000059999","observeOpenTs":"1710000000100","observeCloseTs":"1710000059900"}'
        Expected = @('"marketCodeId":"9001"', '"interval":"1m"', '"close":"105.00"')
    }
    "premium-candle" = @{
        Endpoint = "/api/v1/stream/candles/close?type=premium"
        Topic    = "analytics.premium-candle"
        Payload  = '{"symbol":"E2E-PREMIUM-CANDLE","baseExchangeId":"1","compareExchangeId":"2","interval":"1m","open":"1.00","high":"1.80","low":"0.90","close":"1.50","bucketOpenTs":"1710000000000","bucketCloseTs":"1710000059999","observeOpenTs":"1710000000100","observeCloseTs":"1710000059900"}'
        Expected = @('"symbol":"E2E-PREMIUM-CANDLE"', '"interval":"1m"', '"close":"1.50"')
    }
    "premium-detail-candle" = @{
        Endpoint = "/api/v1/stream/candles/close?type=premium-detail"
        Topic    = "analytics.premium-detail-candle"
        Payload  = '{"symbol":"E2E-PD-CANDLE","baseExchangeId":"1","compareExchangeId":"2","interval":"1m","openBaseVal":"100.00","openBaseQuoteVal":"1000.00","openCompareVal":"99.00","openCompareQuoteVal":"990.00","highBaseVal":"105.00","highBaseQuoteVal":"1050.00","highCompareVal":"104.00","highCompareQuoteVal":"1040.00","lowBaseVal":"98.00","lowBaseQuoteVal":"980.00","lowCompareVal":"97.00","lowCompareQuoteVal":"970.00","closeBaseVal":"103.00","closeBaseQuoteVal":"1030.00","closeCompareVal":"102.00","closeCompareQuoteVal":"1020.00","bucketOpenTs":"1710000000000","bucketCloseTs":"1710000059999","observeOpenTs":"1710000000100","observeCloseTs":"1710000059900"}'
        Expected = @('"symbol":"E2E-PD-CANDLE"', '"interval":"1m"', '"closeBaseVal":"103.00"')
    }
    "tick-indicator" = @{
        Endpoint = "/api/v1/stream/indicators/close?type=tick"
        Topic    = "analytics.tick-indicator"
        Payload  = '{"marketCodeId":"9001","interval":"1m","type":"EMA","period":"20","value":"104.35","bucketOpenTs":"1710000000000","bucketCloseTs":"1710000059999","observeOpenTs":"1710000000100","observeCloseTs":"1710000059900"}'
        Expected = @('"marketCodeId":"9001"', '"type":"EMA"', '"value":"104.35"')
    }
    "premium-indicator" = @{
        Endpoint = "/api/v1/stream/indicators/close?type=premium"
        Topic    = "analytics.premium-indicator"
        Payload  = '{"symbol":"E2E-PREMIUM-IND","baseExchangeId":"1","compareExchangeId":"2","interval":"1m","type":"EMA","period":"20","value":"1.35","bucketOpenTs":"1710000000000","bucketCloseTs":"1710000059999","observeOpenTs":"1710000000100","observeCloseTs":"1710000059900"}'
        Expected = @('"symbol":"E2E-PREMIUM-IND"', '"type":"EMA"', '"value":"1.35"')
    }
}

function Write-Step {
    param([string]$Message)
    Write-Host "[stream-smoke] $Message"
}

function Join-Url {
    param(
        [string]$BaseUrl,
        [string]$Path
    )

    return $BaseUrl.TrimEnd("/") + "/" + $Path.TrimStart("/")
}

function Assert-CommandExists {
    param([string]$CommandName)

    if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
        throw "Required command '$CommandName' was not found. Install it or add it to PATH."
    }
}

function Test-ApiHealth {
    param([string]$BaseUrl)

    if ($SkipHealthCheck) {
        Write-Step "Skipping API health check"
        return
    }

    $healthUrl = Join-Url $BaseUrl "/actuator/health"
    Write-Step "Checking API health at $healthUrl"

    try {
        $response = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
    } catch {
        throw "API health check failed. Start the API first or pass -SkipHealthCheck. URL: $healthUrl. Error: $($_.Exception.Message)"
    }

    if ($null -ne $response.status -and $response.status -ne "UP") {
        throw "API health is '$($response.status)', expected 'UP'."
    }
}

function Test-KafkaContainer {
    param([string]$ContainerName)

    Write-Step "Checking Kafka container '$ContainerName'"
    docker inspect $ContainerName | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Kafka container '$ContainerName' was not found. Start docker/docker-compose.yml first."
    }
}

function Ensure-KafkaTopic {
    param([string]$Topic)

    Write-Step "Ensuring Kafka topic '$Topic'"
    docker exec $KafkaContainer kafka-topics --bootstrap-server $KafkaBootstrap --create --if-not-exists --topic $Topic --partitions 1 --replication-factor 1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create or verify Kafka topic '$Topic'."
    }
}

function Send-KafkaMessage {
    param(
        [string]$Topic,
        [string]$Payload
    )

    Write-Step "Producing one message to '$Topic'"
    $Payload | docker exec -i $KafkaContainer kafka-console-producer --bootstrap-server $KafkaBootstrap --topic $Topic
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to produce a message to Kafka topic '$Topic'."
    }
}

function Start-SseCapture {
    param(
        [string]$Url,
        [string]$OutputFile,
        [int]$ReadTimeoutSeconds
    )

    Set-Content -LiteralPath $OutputFile -Value "" -NoNewline
    $errorFile = "$OutputFile.err"
    Set-Content -LiteralPath $errorFile -Value "" -NoNewline
    $arguments = "--no-buffer --silent --show-error --max-time $ReadTimeoutSeconds -H `"Accept: text/event-stream`" `"$Url`""

    return Start-Process -WindowStyle Hidden -FilePath "curl.exe" -ArgumentList $arguments -RedirectStandardOutput $OutputFile -RedirectStandardError $errorFile -PassThru
}

function Wait-ForText {
    param(
        [string]$OutputFile,
        [string[]]$Expected,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        if (Test-Path -LiteralPath $OutputFile) {
            $content = Get-Content -LiteralPath $OutputFile -Raw -ErrorAction SilentlyContinue
            if ($null -eq $content) {
                $content = ""
            }
            $missing = @($Expected | Where-Object { -not $content.Contains($_) })

            if ($missing.Count -eq 0) {
                return $content
            }
        }

        Start-Sleep -Milliseconds 250
    }

    $lastContent = ""
    if (Test-Path -LiteralPath $OutputFile) {
        $lastContent = Get-Content -LiteralPath $OutputFile -Raw -ErrorAction SilentlyContinue
    }

    throw "Timed out waiting for expected SSE text: $($Expected -join ', '). Last captured content: $lastContent"
}

function Invoke-StreamScenario {
    param(
        [string]$Name,
        [hashtable]$Config
    )

    $url = Join-Url $ApiBaseUrl $Config.Endpoint
    $outputFile = Join-Path ([System.IO.Path]::GetTempPath()) "coindata-stream-smoke-$Name.log"
    $captureProcess = $null

    Write-Step "Running scenario '$Name'"
    Ensure-KafkaTopic $Config.Topic

    try {
        Write-Step "Opening SSE stream $url"
        $captureProcess = Start-SseCapture -Url $url -OutputFile $outputFile -ReadTimeoutSeconds $TimeoutSeconds

        Start-Sleep -Milliseconds 1000
        Send-KafkaMessage -Topic $Config.Topic -Payload $Config.Payload

        [void]$captureProcess.WaitForExit(($TimeoutSeconds + 2) * 1000)
        if (-not $captureProcess.HasExited) {
            Stop-Process -Id $captureProcess.Id -Force -ErrorAction SilentlyContinue
        }

        $captured = Wait-ForText -OutputFile $outputFile -Expected $Config.Expected -TimeoutSeconds 1
        Write-Step "Scenario '$Name' passed"
        Write-Host ""
        Write-Host "Captured SSE output:"
        Write-Host $captured
        Write-Host ""
    } finally {
        if ($null -ne $captureProcess -and -not $captureProcess.HasExited) {
            Stop-Process -Id $captureProcess.Id -Force -ErrorAction SilentlyContinue
        }
    }
}

if ($ListScenarios) {
    Write-Host "Available scenarios:"
    foreach ($name in $Scenarios.Keys) {
        Write-Host "  - $name"
    }
    exit 0
}

Assert-CommandExists "docker"
Test-ApiHealth $ApiBaseUrl
Test-KafkaContainer $KafkaContainer

if ($Scenario -eq "all") {
    foreach ($name in $Scenarios.Keys) {
        Invoke-StreamScenario -Name $name -Config $Scenarios[$name]
    }
} else {
    Invoke-StreamScenario -Name $Scenario -Config $Scenarios[$Scenario]
}

Write-Step "All selected scenarios passed"
