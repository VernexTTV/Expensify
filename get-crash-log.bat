@echo off
echo Downloading ADB platform tools...
echo.

REM Create temp directory
if not exist "%TEMP%\adb-tools" mkdir "%TEMP%\adb-tools"
cd /d "%TEMP%\adb-tools"

REM Download ADB platform tools
echo Downloading from Google...
curl -L -o platform-tools.zip https://dl.google.com/android/repository/platform-tools-latest-windows.zip

echo.
echo Extracting...
tar -xf platform-tools.zip

echo.
echo ADB is ready!
echo.

REM Run ADB
cd platform-tools
echo Clearing logcat...
adb.exe logcat -c

echo.
echo Waiting for crash... Open the app now!
echo.
adb.exe logcat *:E

pause
