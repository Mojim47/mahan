@echo off
echo ========================================
echo   DeliveryTracker - Final Build Guide
echo ========================================
echo.
echo PROBLEM: Java version mismatch
echo - Your system has Java 21 (version 65)
echo - Gradle 7.6 needs Java 11-17
echo.
echo SOLUTIONS:
echo.
echo 1. EASIEST - Use Android Studio:
echo    - Open Android Studio
echo    - File > Open > Select DeliveryTracker folder
echo    - Click "Sync Now"
echo    - Build > Make Project
echo.
echo 2. Install Java 11/17:
echo    - Download from: https://adoptium.net/
echo    - Set JAVA_HOME to Java 11/17 path
echo    - Run: gradlew.bat clean assembleDebug
echo.
echo 3. Use system Gradle (if installed):
echo    - gradle clean assembleDebug
echo.
echo ========================================
echo   Project is READY - just needs compatible Java
echo ========================================
pause