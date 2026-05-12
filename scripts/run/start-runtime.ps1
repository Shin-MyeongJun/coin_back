#requires -Version 5.1
[CmdletBinding()]
param(
    [string[]]$Modules = @("meta_data", "market_data", "analytics", "api"),
    [switch]$All,
    [switch]$IncludeIngestion,
    [switch]$IncludeEconomic,
    [ValidateRange(1, 100)]
    [int]$MarketDataInstances = 1,
    [ValidateRange(1, 100)]
    [int]$AnalyticsInstances = 1,
    [ValidateRange(1, 100)]
    [int]$ApiInstances = 1,
    [ValidateRange(1, 65535)]
    [int]$ApiBasePort = 8080,
    [string]$AppEnv = $env:APP_ENV,
    [string]$LogDir
)

$ErrorActionPreference = "Stop"

$RepoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$GradleWrapper = Join-Path $RepoRoot "gradlew.bat"
if (-not (Test-Path -LiteralPath $GradleWrapper)) {
    throw "Gradle wrapper not found: $GradleWrapper"
}

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $RepoRoot "build\run-logs"
}

$PidDir = Join-Path $RepoRoot "build\run-pids"
$CommandDir = Join-Path $RepoRoot "build\run-commands"
New-Item -ItemType Directory -Force -Path $LogDir, $PidDir, $CommandDir | Out-Null

$ProjectByModule = @{
    "upbit_ingestion"   = ":upbit_ingestion"
    "binance_ingestion" = ":binance_ingestion"
    "fx_ingestion"      = ":fx_ingestion"
    "fred"              = ":fred"
    "crawling"          = ":crawling"
    "meta_data"         = ":meta_data"
    "market_data"       = ":market_data"
    "analytics"         = ":analytics"
    "api"               = ":api"
}

$AllExecutableModules = @(
    "upbit_ingestion",
    "binance_ingestion",
    "fx_ingestion",
    "fred",
    "crawling",
    "meta_data",
    "market_data",
    "analytics",
    "api"
)

$ScalableModules = @("market_data", "analytics", "api")

$ModuleAliases = @{
    "upbit"             = "upbit_ingestion"
    "upbit_ingestion"   = "upbit_ingestion"
    "upbit-ingestion"   = "upbit_ingestion"
    "binance"           = "binance_ingestion"
    "binance_ingestion" = "binance_ingestion"
    "binance-ingestion" = "binance_ingestion"
    "fx"                = "fx_ingestion"
    "fx_ingestion"      = "fx_ingestion"
    "fx-ingestion"      = "fx_ingestion"
    "fred"              = "fred"
    "crawling"          = "crawling"
    "meta"              = "meta_data"
    "meta_data"         = "meta_data"
    "meta-data"         = "meta_data"
    "market"            = "market_data"
    "market_data"       = "market_data"
    "market-data"       = "market_data"
    "analytics"         = "analytics"
    "api"               = "api"
}

function Normalize-ModuleName {
    param([Parameter(Mandatory = $true)][string]$Name)

    $key = $Name.Trim().TrimStart(":").ToLowerInvariant()
    if ($ModuleAliases.ContainsKey($key)) {
        return $ModuleAliases[$key]
    }

    $underscoreKey = $key.Replace("-", "_")
    if ($ModuleAliases.ContainsKey($underscoreKey)) {
        return $ModuleAliases[$underscoreKey]
    }

    if ($ProjectByModule.ContainsKey($underscoreKey)) {
        return $underscoreKey
    }

    $known = ($AllExecutableModules -join ", ")
    throw "Unknown executable module '$Name'. Known modules: $known"
}

function Add-SelectedModule {
    param(
        [Parameter(Mandatory = $true)][AllowEmptyCollection()][System.Collections.Generic.List[string]]$Selected,
        [Parameter(Mandatory = $true)][string]$Name
    )

    $module = Normalize-ModuleName $Name
    if (-not $Selected.Contains($module)) {
        [void]$Selected.Add($module)
    }
}

function Get-DesiredInstanceCount {
    param([Parameter(Mandatory = $true)][string]$Module)

    switch ($Module) {
        "market_data" { return $MarketDataInstances }
        "analytics" { return $AnalyticsInstances }
        "api" { return $ApiInstances }
        default { return 1 }
    }
}

function ConvertTo-PowerShellLiteral {
    param([AllowNull()][string]$Value)

    if ($null -eq $Value) {
        return "''"
    }

    return "'" + ($Value -replace "'", "''") + "'"
}

function Read-LiveRunRecords {
    if (-not (Test-Path -LiteralPath $PidDir)) {
        return @()
    }

    $records = @()
    foreach ($file in Get-ChildItem -LiteralPath $PidDir -Filter "*.json" -File) {
        try {
            $record = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
            $process = Get-Process -Id $record.processId -ErrorAction SilentlyContinue
            if ($null -ne $process) {
                Add-Member -InputObject $record -NotePropertyName pidFile -NotePropertyValue $file.FullName -Force
                $records += $record
            }
        }
        catch {
            Write-Warning "Ignoring invalid pid file: $($file.FullName)"
        }
    }

    return $records
}

function Start-ModuleInstance {
    param(
        [Parameter(Mandatory = $true)][string]$Module,
        [Parameter(Mandatory = $true)][int]$Instance
    )

    $project = $ProjectByModule[$Module]
    $task = "${project}:bootRun"
    $port = $null
    if ($Module -eq "api") {
        $port = $ApiBasePort + $Instance - 1
        if ($port -gt 65535) {
            throw "API port is out of range: $port"
        }
    }

    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $instanceName = if ((Get-DesiredInstanceCount $Module) -gt 1) { "$Module-$Instance" } else { $Module }
    $baseName = "$instanceName-$stamp"
    $stdout = Join-Path $LogDir "$baseName.out.log"
    $stderr = Join-Path $LogDir "$baseName.err.log"
    $commandFile = Join-Path $CommandDir "$baseName.ps1"
    $pidFile = Join-Path $PidDir "$baseName.json"

    $lines = @(
        '$ErrorActionPreference = "Stop"',
        "Set-Location -LiteralPath $(ConvertTo-PowerShellLiteral $RepoRoot)",
        "`$env:COINDATA_RUN_MODULE = $(ConvertTo-PowerShellLiteral $Module)",
        "`$env:COINDATA_RUN_INSTANCE = $(ConvertTo-PowerShellLiteral ([string]$Instance))"
    )

    if (-not [string]::IsNullOrWhiteSpace($AppEnv)) {
        $lines += "`$env:APP_ENV = $(ConvertTo-PowerShellLiteral $AppEnv)"
    }

    if ($Module -eq "api") {
        $lines += "`$env:API_SERVER_PORT = $(ConvertTo-PowerShellLiteral ([string]$port))"
    }

    $lines += "& $(ConvertTo-PowerShellLiteral $GradleWrapper) '--no-daemon' '--console=plain' $(ConvertTo-PowerShellLiteral $task)"
    $lines += 'exit $LASTEXITCODE'

    Set-Content -LiteralPath $commandFile -Value $lines -Encoding UTF8

    $process = Start-Process `
        -WindowStyle Hidden `
        -FilePath "powershell.exe" `
        -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $commandFile) `
        -WorkingDirectory $RepoRoot `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -PassThru

    $record = [ordered]@{
        processId = $process.Id
        module    = $Module
        project   = $project
        task      = $task
        instance  = $Instance
        port      = $port
        startedAt = (Get-Date).ToString("o")
        stdout    = $stdout
        stderr    = $stderr
        command   = $commandFile
    }

    $record | ConvertTo-Json | Set-Content -LiteralPath $pidFile -Encoding UTF8

    $portLabel = if ($null -ne $port) { " port=$port" } else { "" }
    Write-Host "Started $Module instance=$Instance pid=$($process.Id)$portLabel"
    Write-Host "  stdout: $stdout"
    Write-Host "  stderr: $stderr"
}

$SelectedModules = [System.Collections.Generic.List[string]]::new()
$RequestedAll = $All -or (($Modules.Count -eq 1) -and ($Modules[0].Trim().ToLowerInvariant() -eq "all"))

if ($RequestedAll) {
    foreach ($module in $AllExecutableModules) {
        Add-SelectedModule -Selected $SelectedModules -Name $module
    }
}
else {
    foreach ($module in $Modules) {
        Add-SelectedModule -Selected $SelectedModules -Name $module
    }
}

if ($IncludeIngestion) {
    foreach ($module in @("upbit_ingestion", "binance_ingestion", "fx_ingestion")) {
        Add-SelectedModule -Selected $SelectedModules -Name $module
    }
}

if ($IncludeEconomic) {
    foreach ($module in @("fred", "crawling")) {
        Add-SelectedModule -Selected $SelectedModules -Name $module
    }
}

if ($SelectedModules.Count -eq 0) {
    throw "No modules selected."
}

$liveRecords = @(Read-LiveRunRecords)

foreach ($module in $SelectedModules) {
    $desired = Get-DesiredInstanceCount $module
    $isScalable = $ScalableModules -contains $module

    if (($desired -gt 1) -and (-not $isScalable)) {
        throw "$module is single-instance by policy. Only market_data, analytics, and api can run multiple instances."
    }

    $liveForModule = @($liveRecords | Where-Object { $_.module -eq $module })
    if ((-not $isScalable) -and ($liveForModule.Count -ge 1)) {
        Write-Host "Skipping $module; one live instance already exists. Use stop-runtime.ps1 first to restart it."
        continue
    }

    if ($liveForModule.Count -ge $desired) {
        Write-Host "Skipping $module; live instances=$($liveForModule.Count), desired=$desired."
        continue
    }

    for ($instance = $liveForModule.Count + 1; $instance -le $desired; $instance++) {
        Start-ModuleInstance -Module $module -Instance $instance
    }
}

Write-Host "Done. PID files: $PidDir"
