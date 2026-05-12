# start-frontend.ps1
# Stops any old frontend on port 5173, starts Vite dev server

Write-Host "" -ForegroundColor Cyan
Write-Host "=== Next2View Frontend Starter ===" -ForegroundColor Cyan
Write-Host ""

# 1. Stop old frontend if running on 5173
$conn = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    $procId = $conn[0].OwningProcess
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    Write-Host "[!] Stopping existing frontend (PID $procId, $($proc.Name))" -ForegroundColor Yellow
    Stop-Process -Id $procId -Force
    Start-Sleep -Seconds 1
    Write-Host "[OK] Old process stopped" -ForegroundColor Green
} else {
    Write-Host "[OK] Port 5173 is free" -ForegroundColor Green
}

# 2. Start frontend
Write-Host ""
Write-Host "[*] Starting Vite dev server..." -ForegroundColor Cyan
Write-Host "    Will open at http://localhost:5173" -ForegroundColor DarkGray
Write-Host "    Press Ctrl+C to stop." -ForegroundColor DarkGray
Write-Host ""
Push-Location (Join-Path $PSScriptRoot '..\frontend')
try {
    npm run dev
} finally {
    Pop-Location
}
