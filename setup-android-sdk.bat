@echo off
echo "=== 快速配置Android SDK ==="

REM 设置环境变量
set ANDROID_SDK_ROOT=%cd%\android-sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%
set PATH=%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin;%ANDROID_SDK_ROOT%\platform-tools;%PATH%

echo 当前SDK路径: %ANDROID_SDK_ROOT%

REM 检查cmdline-tools是否存在
if not exist "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" (
    echo 错误: 找不到sdkmanager.bat
    echo 请确认cmdline-tools已正确配置
    pause
    exit /b 1
)

echo "=== 开始下载Android组件（需要网络连接）==="
echo 这可能需要几分钟时间...

REM 接受许可证
echo y | "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" --licenses

REM 安装必要组件
echo 安装Platform Tools...
"%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools"

echo 安装Android 33 Platform...
"%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "platforms;android-33"

echo 安装Build Tools...
"%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "build-tools;33.0.0"

echo "=== 验证安装结果 ==="
if exist "%ANDROID_SDK_ROOT%\platform-tools" (
    echo ✓ Platform Tools 安装成功
) else (
    echo ✗ Platform Tools 安装失败
)

if exist "%ANDROID_SDK_ROOT%\platforms" (
    echo ✓ Android Platforms 安装成功
) else (
    echo ✗ Android Platforms 安装失败
)

if exist "%ANDROID_SDK_ROOT%\build-tools" (
    echo ✓ Build Tools 安装成功
) else (
    echo ✗ Build Tools 安装失败
)

echo "=== 配置完成 ==="
echo 如果所有组件都显示成功，现在可以构建APK了！
pause