#requires -Version 5.1
[CmdletBinding()]
param(
    [string[]]$Modules = @(),
    [switch]$All,
    [switch]$KeepPidFiles
)

$ErrorActionPreference = "Stop"

$RepoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$PidDir = Join-Path $RepoRoot "build\run-pids"

$ExecutableModules = @(
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

    $known = ($ExecutableModules -join ", ")
    throw "Unknown executable module '$Name'. Known modules: $known"
}

function Stop-ProcessTree {
    param([Parameter(Mandatory = $true)][int]$ProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId ([int]$child.ProcessId)
    }

    $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if ($null -ne $process) {
        Stop-Process -Id $ProcessId -Force
    }
}

if (-not (Test-Path -LiteralPath $PidDir)) {
    Write-Host "No PID directory found: $PidDir"
    return
}

$selected = @()
if ($All -or $Modules.Count -eq 0) {
    $selected = $ExecutableModules
}
else {
    $selected = @($Modules | ForEach-Object { Normalize-ModuleName $_ } | Select-Object -Unique)
}

$pidFiles = @(Get-ChildItem -LiteralPath $PidDir -Filter "*.json" -File)
if ($pidFiles.Count -eq 0) {
    Write-Host "No runtime PID files found."
    return
}

foreach ($file in $pidFiles) {
    try {
        $record = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
    }
    catch {
        Write-Warning "Ignoring invalid pid file: $($file.FullName)"
        continue
    }

    if ($selected -notcontains $record.module) {
        continue
    }

    $process = Get-Process -Id $record.processId -ErrorAction SilentlyContinue
    if ($null -eq $process) {
        Write-Host "Already stopped $($record.module) pid=$($record.processId)"
    }
    else {
        Stop-ProcessTree -ProcessId ([int]$record.processId)
        Write-Host "Stopped $($record.module) pid=$($record.processId)"
    }

    if (-not $KeepPidFiles) {
        Remove-Item -LiteralPath $file.FullName -Force
    }
}
