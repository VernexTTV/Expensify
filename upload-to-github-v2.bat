@echo off
echo ========================================
echo   Expense Tracker - GitHub Upload
echo ========================================
echo.

REM Check if Git is installed
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Git is not installed!
    echo Please install Git from: https://git-scm.com/downloads
    pause
    exit /b 1
)

echo Git is installed: 
git --version
echo.

REM Check if Git is configured
git config --global user.name >nul 2>&1
if %errorlevel% neq 0 (
    echo Git is not configured yet. Let's set it up!
    echo.
    set /p GIT_NAME="Enter your name (e.g., John Doe): "
    set /p GIT_EMAIL="Enter your email (e.g., john@example.com): "
    
    git config --global user.name "!GIT_NAME!"
    git config --global user.email "!GIT_EMAIL!"
    
    echo.
    echo Git configured successfully!
    echo.
)

REM Get GitHub username
set /p GITHUB_USERNAME="Enter your GitHub username: "
if "%GITHUB_USERNAME%"=="" (
    echo ERROR: Username cannot be empty!
    pause
    exit /b 1
)

echo.
echo Repository will be created at:
echo https://github.com/%GITHUB_USERNAME%/expense-tracker
echo.
echo IMPORTANT: Make sure you have created the repository on GitHub first!
echo   1. Go to: https://github.com/new
echo   2. Repository name: expense-tracker
echo   3. Make it Public
echo   4. Don't add README, .gitignore, or license
echo   5. Click "Create repository"
echo.
set /p CONTINUE="Have you created the repository? (Y/N): "
if /i not "%CONTINUE%"=="Y" (
    echo.
    echo Please create the repository first, then run this script again.
    pause
    exit /b 0
)

echo.
echo Initializing Git repository...
git init

echo.
echo Adding files...
git add .

echo.
echo Creating commit...
git commit -m "Initial commit - Expense Tracker App"

if %errorlevel% neq 0 (
    echo.
    echo Commit failed. This might be because there are no changes.
    echo Continuing anyway...
)

echo.
echo Setting branch to main...
git branch -M main

echo.
echo Adding remote repository...
git remote remove origin 2>nul
git remote add origin https://github.com/%GITHUB_USERNAME%/expense-tracker.git

echo.
echo Pushing to GitHub...
echo (You may be asked to enter your GitHub credentials)
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
    echo   1. Go to: https://github.com/%GITHUB_USERNAME%/expense-tracker
    echo   2. Click the "Actions" tab
    echo   3. Wait for the build to complete (3-5 minutes)
    echo   4. Download the APK from "Artifacts"
    echo.
    echo The APK will be automatically built by GitHub Actions!
    echo.
) else (
    echo.
    echo ========================================
    echo   ERROR!
    echo ========================================
    echo.
    echo Failed to push to GitHub.
    echo.
    echo Common solutions:
    echo   1. Make sure you created the repository on GitHub
    echo   2. Check your GitHub credentials
    echo   3. Try the manual method (see GITHUB_BUILD_GUIDE.md)
    echo.
)

pause
