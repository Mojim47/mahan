@echo off
echo Quick Build for DeliveryTracker...

REM Check if gradle is installed
gradle --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Gradle not found. Please install Gradle or use Android Studio.
    echo Download from: https://gradle.org/install/
    pause
    exit /b 1
)

echo Building with system Gradle...
gradle clean assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ✓ Build successful!
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo ✗ Build failed!
)

pause