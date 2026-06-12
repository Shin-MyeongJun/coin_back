param(
    [Parameter(Mandatory)][string]$LogPath,
    [string]$Label = ""
)

if (-not (Test-Path $LogPath)) {
    Write-Host "File not found: $LogPath" -ForegroundColor Red
    return
}

$pauses = @()
Get-Content $LogPath | ForEach-Object {
    if ($_ -match 'Pause (Young|Mixed|Full)[^0-9]*?(\d+\.\d+)ms') {
        $pauses += [PSCustomObject]@{
            Type = $matches[1]
            DurationMs = [double]$matches[2]
        }
    }
}

if ($pauses.Count -eq 0) {
    Write-Host "[$LogPath] No GC pause data found" -ForegroundColor Yellow
    return
}

$display = if ($Label) { $Label } else { Split-Path $LogPath -Leaf }
Write-Host ""
Write-Host "=== $display ===" -ForegroundColor Cyan
Write-Host "Total GC pauses: $($pauses.Count)"

function Get-Percentile($sortedArr, $p) {
    $idx = [int][Math]::Min($sortedArr.Count - 1, [Math]::Floor($sortedArr.Count * $p))
    return $sortedArr[$idx].DurationMs
}

$grouped = $pauses | Group-Object Type
foreach ($g in $grouped) {
    $sorted = $g.Group | Sort-Object DurationMs
    $sum = ($g.Group | Measure-Object DurationMs -Sum).Sum
    $avg = $sum / $g.Count
    $p50 = Get-Percentile $sorted 0.5
    $p95 = Get-Percentile $sorted 0.95
    $p99 = Get-Percentile $sorted 0.99
    $max = ($sorted | Select-Object -Last 1).DurationMs

    Write-Host ("{0,-8} count={1,5}  sum={2,10:F1}ms  avg={3,6:F2}ms  p50={4,6:F2}  p95={5,6:F2}  p99={6,6:F2}  max={7,7:F2}" `
        -f $g.Name, $g.Count, $sum, $avg, $p50, $p95, $p99, $max)
}

$totalSum = ($pauses | Measure-Object DurationMs -Sum).Sum
Write-Host ""
Write-Host ("Total GC pause time: {0:F1} ms" -f $totalSum) -ForegroundColor Green
