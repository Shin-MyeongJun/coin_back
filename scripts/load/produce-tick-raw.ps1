#requires -Version 7.0
[CmdletBinding()]
param(
    [ValidateRange(1, [int]::MaxValue)]
    [int]$TargetTps = 15000,
    [ValidateRange(1, [int]::MaxValue)]
    [int]$DurationSec = 120,
    [string]$BootstrapServers = "localhost:9092",
    [string]$Topic = "ingestion-exchange.tick-raw"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$RepoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
$SourceFile = Join-Path $PSScriptRoot "LoadProducer.java"
$ContractsSourceRoot = Join-Path $RepoRoot "modules\contracts\src\main\java"
$TickRawSource = Join-Path $ContractsSourceRoot "com\example\demo\contracts\message\raw\TickRawMessage.java"
$ClassesDir = Join-Path $RepoRoot "build\load\classes"
$GradleCache = Join-Path $env:USERPROFILE ".gradle\caches\modules-2\files-2.1"
$PathSeparator = [System.IO.Path]::PathSeparator

function Find-LatestJar {
    param(
        [Parameter(Mandatory = $true)][string]$ModulePath,
        [Parameter(Mandatory = $true)][string]$Pattern
    )

    $root = Join-Path $GradleCache $ModulePath
    if (-not (Test-Path -LiteralPath $root)) {
        return $null
    }

    return Get-ChildItem -LiteralPath $root -Recurse -Filter $Pattern -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch "-(test|sources|javadoc)\.jar$" } |
        Sort-Object FullName -Descending |
        Select-Object -First 1
}

function Resolve-DependencyJars {
    $kafkaClients = Find-LatestJar -ModulePath "org.apache.kafka\kafka-clients" -Pattern "kafka-clients-*.jar"
    $slf4jApi = Find-LatestJar -ModulePath "org.slf4j\slf4j-api" -Pattern "slf4j-api-*.jar"

    if ($null -eq $kafkaClients -or $null -eq $slf4jApi) {
        $gradleWrapper = Join-Path $RepoRoot "gradlew.bat"
        if (-not (Test-Path -LiteralPath $gradleWrapper)) {
            throw "Gradle wrapper not found: $gradleWrapper"
        }

        Write-Host "Resolving Kafka client dependencies through Gradle..."
        & $gradleWrapper "--no-daemon" "--console=plain" ":market_data:compileJava"
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle dependency resolution failed with exit code $LASTEXITCODE"
        }

        $kafkaClients = Find-LatestJar -ModulePath "org.apache.kafka\kafka-clients" -Pattern "kafka-clients-*.jar"
        $slf4jApi = Find-LatestJar -ModulePath "org.slf4j\slf4j-api" -Pattern "slf4j-api-*.jar"
    }

    if ($null -eq $kafkaClients) {
        throw "kafka-clients jar was not found in Gradle cache: $GradleCache"
    }
    if ($null -eq $slf4jApi) {
        throw "slf4j-api jar was not found in Gradle cache: $GradleCache"
    }

    $optionalJars = @(
        (Find-LatestJar -ModulePath "org.lz4\lz4-java" -Pattern "lz4-java-*.jar"),
        (Find-LatestJar -ModulePath "org.xerial.snappy\snappy-java" -Pattern "snappy-java-*.jar"),
        (Find-LatestJar -ModulePath "com.github.luben\zstd-jni" -Pattern "zstd-jni-*.jar")
    ) | Where-Object { $null -ne $_ } | ForEach-Object { $_.FullName }

    return @($kafkaClients.FullName, $slf4jApi.FullName) + $optionalJars
}

function Test-CompileRequired {
    $classFile = Join-Path $ClassesDir "LoadProducer.class"
    if (-not (Test-Path -LiteralPath $classFile)) {
        return $true
    }

    $classTimestamp = (Get-Item -LiteralPath $classFile).LastWriteTimeUtc
    $sourceTimestamp = (Get-Item -LiteralPath $SourceFile).LastWriteTimeUtc
    $contractTimestamp = (Get-Item -LiteralPath $TickRawSource).LastWriteTimeUtc
    return $sourceTimestamp -gt $classTimestamp -or $contractTimestamp -gt $classTimestamp
}

if (-not (Test-Path -LiteralPath $SourceFile)) {
    throw "LoadProducer source not found: $SourceFile"
}
if (-not (Test-Path -LiteralPath $TickRawSource)) {
    throw "TickRawMessage source not found: $TickRawSource"
}

$dependencyJars = @(Resolve-DependencyJars)
New-Item -ItemType Directory -Force -Path $ClassesDir | Out-Null

if (Test-CompileRequired) {
    $compileClasspath = $dependencyJars -join $PathSeparator
    $sourcePath = @($PSScriptRoot, $ContractsSourceRoot) -join $PathSeparator
    $javacArgs = @(
        "-encoding", "UTF-8",
        "-cp", $compileClasspath,
        "-sourcepath", $sourcePath,
        "-d", $ClassesDir,
        $SourceFile,
        $TickRawSource
    )

    Write-Host "Compiling scripts/load/LoadProducer.java..."
    & javac @javacArgs
    if ($LASTEXITCODE -ne 0) {
        throw "javac failed with exit code $LASTEXITCODE"
    }
}

$runtimeClasspath = (@($ClassesDir) + $dependencyJars) -join $PathSeparator
$javaArgs = @(
    "-Dfile.encoding=UTF-8",
    "-cp", $runtimeClasspath,
    "LoadProducer",
    "--target-tps", $TargetTps.ToString([System.Globalization.CultureInfo]::InvariantCulture),
    "--duration-sec", $DurationSec.ToString([System.Globalization.CultureInfo]::InvariantCulture),
    "--bootstrap-servers", $BootstrapServers,
    "--topic", $Topic
)

& java @javaArgs
exit $LASTEXITCODE
