# Download ADB and get crash log
Write-Host "Setting up ADB to capture crash log..." -ForegroundColor Yellow
Write-Host ""

# Create temp directory
$tempDir = "$env:TEMP\adb-portable"
if (-not (Test-Path $tempDir)) {
    New-Item -ItemType Directory -Path $tempDir | Out-Null
}

Set-Location $tempDir

# Download ADB if not already present
if (-not (Test-Path ".\platform-tools\adb.exe")) {
    Write-Host "Downloading ADB Platform Tools..." -ForegroundColor Cyan
    $url = "https://dl.google.com/android/repository/platform-tools-latest-windows.zip"
    Invoke-WebRequest -Uri $url -OutFile "platform-tools.zip"
    
    Write-Host "Extracting..." -ForegroundColor Cyan
    Expand-Archive -Path "platform-tools.zip" -DestinationPath "." -Force
    Remove-Item "platform-tools.zip"
}

Set-Location "platform-tools"

Write-Host ""
Write-Host "ADB Ready!" -ForegroundColor Green
Write-Host ""
Write-Host "Checking if phone is connected..." -ForegroundColor Cyan
.\adb.exe devices

Write-Host ""
Write-Host "Clearing old logs..." -ForegroundColor Cyan
.\adb.exe logcat -c

Write-Host ""
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "  OPEN THE APP NOW - Waiting for crash log..." -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host ""

# Capture crash
.\adb.exe logcat *:E

Read-Host "Press Enter to exit"
