@echo off
chcp 65001 >nul
echo ===============================
echo    快速配置Android SDK
echo ===============================

set ANDROID_SDK_ROOT=%cd%\android-sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%

echo 当前SDK路径: %ANDROID_SDK_ROOT%
echo.

echo 检查cmdline-tools是否存在...
if not exist "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" (
    echo [错误] 找不到sdkmanager.bat
    echo 请确认cmdline-tools已正确配置在以下路径:
    echo %ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\
    echo.
    echo 按任意键退出...
    pause >nul
    exit /b 1
)

echo [成功] 找到sdkmanager工具
echo.

echo 开始下载Android组件...
echo 这可能需要几分钟时间，请耐心等待...
echo.

echo 步骤1: 接受许可证...
echo y | "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" --licenses >nul 2>&1

echo 步骤2: 安装Platform Tools...
"%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools"

echo 步骤3: 安装Android 33 Platform...
"%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "platforms;android-33"

echo 步骤4: 安装Build Tools...
"%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "build-tools;33.0.0"

echo.
echo ===============================
echo        验证安装结果
echo ===============================

if exist "%ANDROID_SDK_ROOT%\platform-tools" (
    echo [成功] Platform Tools 安装成功
) else (
    echo [失败] Platform Tools 安装失败
)

if exist "%ANDROID_SDK_ROOT%\platforms" (
    echo [成功] Android Platforms 安装成功
) else (
    echo [失败] Android Platforms 安装失败
)

if exist "%ANDROID_SDK_ROOT%\build-tools" (
    echo [成功] Build Tools 安装成功
) else (
    echo [失败] Build Tools 安装失败
)

echo.
echo ===============================
echo        配置完成
echo ===============================
echo 如果所有组件都显示成功，现在可以构建APK了！
echo.
echo 按任意键继续...
pause >nul