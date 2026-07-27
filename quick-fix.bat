@echo off
echo ========================================
echo   Quick Fix - Configure Git and Upload
echo ========================================
echo.

echo Let's configure Git first...
echo.

set /p GIT_NAME="Enter your name (e.g., Arnav Bansal): "
set /p GIT_EMAIL="Enter your email: "

echo.
echo Configuring Git...
git config --global user.name "%GIT_NAME%"
git config --global user.email "%GIT_EMAIL%"

echo.
echo Git configured successfully!
echo   Name: %GIT_NAME%
echo   Email: %GIT_EMAIL%
echo.

echo Now let's upload to GitHub...
echo.

echo Adding files...
git add .

echo Creating commit...
git commit -m "Initial commit - Expense Tracker App"

echo Pushing to GitHub...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCESS!
    echo ========================================
    echo.
    echo Your code has been uploaded to GitHub!
    echo.
    echo Next steps:
    echo   1. Go to: https://github.com/VernexTTV/expense-tracker
    echo   2. Click the "Actions" tab
    echo   3. Wait for the build to complete
    echo   4. Download the APK from "Artifacts"
    echo.
) else (
    echo.
    echo ========================================
    echo   ERROR!
    echo ========================================
    echo.
    echo Push failed. You may need to authenticate.
    echo.
    echo Try these commands manually:
    echo   1. git push -u origin main
    echo   2. Enter your GitHub username: VernexTTV
    echo   3. Enter your password (or personal access token)
    echo.
)

pause
