@echo off
echo Building DeliveryTracker...

REM Download gradle wrapper if missing
if not exist gradle\wrapper\gradle-wrapper.jar (
    echo Downloading gradle wrapper...
    powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.4-bin.zip' -OutFile 'gradle.zip'"
    powershell -Command "Expand-Archive -Path 'gradle.zip' -DestinationPath 'temp'"
    xcopy temp\gradle-8.4\lib\gradle-launcher-8.4.jar gradle\wrapper\gradle-wrapper.jar /Y
    del gradle.zip
    rmdir /s /q temp
)

REM Build the project
echo Starting build...
java -cp gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain clean assembleDebug

echo Build complete!
pause