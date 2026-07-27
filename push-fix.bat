@echo off
echo ========================================
echo   Pushing Fixes to GitHub
echo ========================================
echo.

echo This will fix all build errors and restart the build.
echo.

cd "C:\Users\DELL\Documents\Arnav AI Projects\Daily Spends\ExpenseTracker"

echo Adding updated files...
git add .github/workflows/build-apk.yml
git add build.gradle

echo Creating commit...
git commit -m "Fix: Update to latest GitHub Actions and fix deprecated Gradle config"

echo Pushing to GitHub...
git push

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCESS!
    echo ========================================
    echo.
    echo Fixes pushed! The build will restart automatically.
    echo.
    echo Go to: https://github.com/VernexTTV/expense-tracker/actions
    echo Watch for the new build to start!
    echo.
) else (
    echo.
    echo ERROR: Failed to push fixes.
    echo.
)

pause
