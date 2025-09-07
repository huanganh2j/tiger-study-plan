@echo off
echo Starting Android SDK setup...

set ANDROID_SDK_ROOT=%cd%\android-sdk
echo SDK Path: %ANDROID_SDK_ROOT%

if not exist "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" (
    echo ERROR: sdkmanager.bat not found
    pause
    exit /b 1
)

echo Installing Platform Tools...
call "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools"

echo Installing Android API 33...
call "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "platforms;android-33" 

echo Installing Build Tools...
call "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "build-tools;33.0.0"

echo Setup completed!
pause