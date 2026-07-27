@echo off
echo ========================================
echo   Pushing Final Fixes to GitHub
echo ========================================
echo.

echo Pushing:
echo   1. Missing gradle-wrapper.jar (CRITICAL!)
echo   2. Updated workflow file (cleaner build process)
echo.

cd "C:\Users\DELL\Documents\Arnav AI Projects\Daily Spends\ExpenseTracker"

echo Adding all files...
git add gradle/wrapper/gradle-wrapper.jar
git add .github/workflows/build-apk.yml

echo Creating commit...
git commit -m "Add gradle-wrapper.jar and update workflow for reliable builds"

echo Pushing to GitHub...
git push

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCESS!
    echo ========================================
    echo.
    echo All fixes pushed! Build should work now!
    echo.
    echo Go to: https://github.com/VernexTTV/expense-tracker/actions
    echo.
    echo The new build should:
    echo   - Download all dependencies automatically
    echo   - Build the APK successfully
    echo   - Show a GREEN checkmark when done!
    echo.
) else (
    echo.
    echo ERROR: Failed to push.
    echo.
)

pause
