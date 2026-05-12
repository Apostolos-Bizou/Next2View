# stop-all.ps1
# Kills all local Next2View processes (backend on 8080, frontend on 5173)

Write-Host "" -ForegroundColor Cyan
Write-Host "=== Next2View Stop All ===" -ForegroundColor Cyan
Write-Host ""

foreach ($port in @(8080, 5173)) {
    $label = if ($port -eq 8080) { "backend" } else { "frontend" }
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $procId = $conn[0].OwningProcess
        $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
        Write-Host "[!] Stopping $label on port $port (PID $procId, $($proc.Name))" -ForegroundColor Yellow
        Stop-Process -Id $procId -Force
        Write-Host "[OK] $label stopped" -ForegroundColor Green
    } else {
        Write-Host "[--] No $label running on port $port" -ForegroundColor DarkGray
    }
}

Write-Host ""
Write-Host "Done." -ForegroundColor Cyan
