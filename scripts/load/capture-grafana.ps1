#requires -Version 7.0
[CmdletBinding()]
param(
    [string]$GrafanaBaseUrl = "http://localhost:3000",
    [string]$OutputDir,
    [ValidateRange(320, 7680)]
    [int]$Width = 1920,
    [ValidateRange(240, 4320)]
    [int]$Height = 1080,
    [string[]]$PanelName = @(),
    [string]$CaptureStamp = (Get-Date -Format "yyyyMMdd-HHmm")
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$RepoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $RepoRoot "docs\portfolio\screenshots\grafana"
}

$GrafanaBaseUrl = $GrafanaBaseUrl.TrimEnd("/")
$GrafanaUser = if ([string]::IsNullOrWhiteSpace($env:GRAFANA_USER)) { "admin" } else { $env:GRAFANA_USER }
$GrafanaPass = if ([string]::IsNullOrWhiteSpace($env:GRAFANA_PASS)) { "admin" } else { $env:GRAFANA_PASS }
$Credential = [System.Management.Automation.PSCredential]::new(
    $GrafanaUser,
    (ConvertTo-SecureString $GrafanaPass -AsPlainText -Force)
)

function ConvertTo-Slug {
    param([Parameter(Mandatory = $true)][string]$Value)

    $slug = $Value.ToLowerInvariant() -replace "[^a-z0-9]+", "-"
    return $slug.Trim("-")
}

function Get-DashboardPanels {
    param([Parameter(Mandatory = $true)]$Panels)

    $result = @()
    foreach ($panel in @($Panels)) {
        if ($null -ne $panel.panels) {
            $result += Get-DashboardPanels -Panels $panel.panels
        }
        if ($null -ne $panel.id -and -not [string]::IsNullOrWhiteSpace($panel.title)) {
            $result += $panel
        }
    }
    return $result
}

function Resolve-PanelId {
    param(
        [Parameter(Mandatory = $true)]$Dashboard,
        [Parameter(Mandatory = $true)][string[]]$Titles
    )

    $panels = Get-DashboardPanels -Panels $Dashboard.panels
    foreach ($title in $Titles) {
        $match = $panels | Where-Object { $_.title -eq $title } | Select-Object -First 1
        if ($null -ne $match) {
            return [int]$match.id
        }
    }
    return $null
}

function Test-Requested {
    param(
        [Parameter(Mandatory = $true)][string]$FriendlyName,
        [Parameter(Mandatory = $true)][string]$OutputName
    )

    if ($PanelName.Count -eq 0) {
        return $true
    }

    $normalizedOutput = ConvertTo-Slug $OutputName
    foreach ($requested in $PanelName) {
        if ($requested -eq $FriendlyName -or (ConvertTo-Slug $requested) -eq $normalizedOutput) {
            return $true
        }
    }
    return $false
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$dashboardTargets = @(
    [pscustomobject]@{
        Path = Join-Path $RepoRoot "docker\observability\grafana\dashboards\coindata-infra-overview.json"
        Uid = "coindata-infra-overview"
        Slug = "coindata-infra-overview"
        Optional = $false
        Panels = @(
            [pscustomobject]@{ FriendlyName = "Kafka Consumer Lag by Topic"; Titles = @("Kafka Consumer Lag by Topic"); OutputName = "kafka-consumer-lag-by-topic" },
            [pscustomobject]@{ FriendlyName = "Kafka Consumer Lag by Group"; Titles = @("Kafka Consumer Lag by Group"); OutputName = "kafka-consumer-lag-by-group" },
            [pscustomobject]@{ FriendlyName = "Redis Memory Usage"; Titles = @("Redis Memory Usage", "Redis Memory"); OutputName = "redis-memory-usage" },
            [pscustomobject]@{ FriendlyName = "Redis Command Throughput"; Titles = @("Redis Command Throughput"); OutputName = "redis-command-throughput" }
        )
    },
    [pscustomobject]@{
        Path = Join-Path $RepoRoot "docker\observability\grafana\dashboards\coindata-api-overview.json"
        Uid = "coindata-api-overview"
        Slug = "coindata-api-overview"
        Optional = $true
        Panels = @(
            [pscustomobject]@{ FriendlyName = "API JVM Memory Used"; Titles = @("API JVM Memory Used"); OutputName = "api-jvm-memory-used" },
            [pscustomobject]@{ FriendlyName = "API JVM GC Pause"; Titles = @("API JVM GC Pause"); OutputName = "api-jvm-gc-pause" },
            [pscustomobject]@{ FriendlyName = "API Process CPU Usage"; Titles = @("API Process CPU Usage"); OutputName = "api-process-cpu-usage" },
            [pscustomobject]@{ FriendlyName = "API HTTP p95 Latency"; Titles = @("API HTTP p95 Latency"); OutputName = "api-http-p95-latency" },
            [pscustomobject]@{ FriendlyName = "API Kafka Records Consumed"; Titles = @("API Kafka Records Consumed"); OutputName = "api-kafka-records-consumed" }
        )
    }
)

$captures = @()
foreach ($dashboardTarget in $dashboardTargets) {
    if (-not (Test-Path -LiteralPath $dashboardTarget.Path)) {
        if ($dashboardTarget.Optional) {
            continue
        }
        throw "Dashboard JSON not found: $($dashboardTarget.Path)"
    }

    $dashboard = Get-Content -LiteralPath $dashboardTarget.Path -Raw | ConvertFrom-Json
    foreach ($panelTarget in $dashboardTarget.Panels) {
        if (-not (Test-Requested -FriendlyName $panelTarget.FriendlyName -OutputName $panelTarget.OutputName)) {
            continue
        }

        $panelId = Resolve-PanelId -Dashboard $dashboard -Titles $panelTarget.Titles
        if ($null -eq $panelId) {
            if ($dashboardTarget.Optional) {
                Write-Host "Skipping optional panel: $($panelTarget.FriendlyName)"
                continue
            }
            throw "Panel not found in $($dashboardTarget.Path): $($panelTarget.FriendlyName)"
        }

        $fileName = "$($panelTarget.OutputName)-$CaptureStamp.png"
        $outputPath = Join-Path $OutputDir $fileName
        $query = "orgId=1&panelId=$panelId&from=now-10m&to=now&width=$Width&height=$Height&tz=browser"
        $uri = "$GrafanaBaseUrl/render/d-solo/$($dashboardTarget.Uid)/$($dashboardTarget.Slug)?$query"

        Invoke-WebRequest `
            -Uri $uri `
            -Authentication Basic `
            -Credential $Credential `
            -OutFile $outputPath `
            -TimeoutSec 120 | Out-Null

        $file = Get-Item -LiteralPath $outputPath
        if ($file.Length -le 0) {
            throw "Grafana render returned an empty file: $outputPath"
        }

        Write-Host "Captured $($panelTarget.FriendlyName) -> $outputPath"
        $captures += [pscustomobject]@{
            DashboardUid = $dashboardTarget.Uid
            PanelId = $panelId
            PanelName = $panelTarget.FriendlyName
            FilePath = $outputPath
            Width = $Width
            Height = $Height
            From = "now-10m"
            To = "now"
        }
    }
}

if ($captures.Count -eq 0) {
    throw "No Grafana panels were captured."
}

$captures
