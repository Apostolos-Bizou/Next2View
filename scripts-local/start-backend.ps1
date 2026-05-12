# start-backend.ps1
# Stops any old backend on port 8080, sources env vars, starts Spring Boot

Write-Host "" -ForegroundColor Cyan
Write-Host "=== Next2View Backend Starter ===" -ForegroundColor Cyan
Write-Host ""

# 1. Stop old backend if running on 8080
$conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    $procId = $conn[0].OwningProcess
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    Write-Host "[!] Stopping existing backend (PID $procId, $($proc.Name))" -ForegroundColor Yellow
    Stop-Process -Id $procId -Force
    Start-Sleep -Seconds 2
    Write-Host "[OK] Old process stopped" -ForegroundColor Green
} else {
    Write-Host "[OK] Port 8080 is free" -ForegroundColor Green
}

# 2. Source env vars
Write-Host ""
Write-Host "[*] Loading local env vars..." -ForegroundColor Cyan
. (Join-Path $env:USERPROFILE '.next2view-local\setup-local-env.ps1')

# 3. Start backend
Write-Host ""
Write-Host "[*] Starting Spring Boot..." -ForegroundColor Cyan
Write-Host "    Watch for: Started Next2ViewApplication in X seconds" -ForegroundColor DarkGray
Write-Host "    Press Ctrl+C to stop the backend." -ForegroundColor DarkGray
Write-Host ""
Push-Location (Join-Path $PSScriptRoot '..\backend')
try {
    mvn spring-boot:run
} finally {
    Pop-Location
}
