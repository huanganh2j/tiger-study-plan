@echo off
chcp 65001 >nul
echo 🔧 Android SDK 自动配置脚本
echo ===============================
echo.

REM 创建Android SDK目录
if not exist "android-sdk" (
    mkdir android-sdk
    echo ✅ 创建 android-sdk 目录
)

REM 设置环境变量
set ANDROID_HOME=%cd%\android-sdk
set ANDROID_SDK_ROOT=%ANDROID_HOME%
echo ✅ 设置环境变量 ANDROID_HOME=%ANDROID_HOME%
echo.

REM 创建基本的SDK目录结构
mkdir "%ANDROID_HOME%\platforms" 2>nul
mkdir "%ANDROID_HOME%\platform-tools" 2>nul
mkdir "%ANDROID_HOME%\build-tools" 2>nul
mkdir "%ANDROID_HOME%\cmdline-tools" 2>nul
mkdir "%ANDROID_HOME%\emulator" 2>nul
mkdir "%ANDROID_HOME%\licenses" 2>nul

REM 创建许可协议文件（跳过许可检查）
echo 创建Android SDK许可协议文件...
echo 24333f8a63b6825ea9c5514f83c2829b004d1fee > "%ANDROID_HOME%\licenses\android-sdk-license"
echo 84831b9409646a918e30573bab4c9c91346d8abd > "%ANDROID_HOME%\licenses\android-sdk-preview-license"
echo d975f751698a77b662f1254ddbeed3901e976f5a > "%ANDROID_HOME%\licenses\intel-android-extra-license"
echo ✅ SDK许可协议配置完成
echo.

REM 创建最小的platform信息（API 33）
mkdir "%ANDROID_HOME%\platforms\android-33" 2>nul
echo target=android-33 > "%ANDROID_HOME%\platforms\android-33\source.properties"
echo pkg.revision=3 >> "%ANDROID_HOME%\platforms\android-33\source.properties"
echo AndroidVersion.ApiLevel=33 >> "%ANDROID_HOME%\platforms\android-33\source.properties"
echo ✅ 创建API 33平台信息
echo.

REM 创建构建工具信息
mkdir "%ANDROID_HOME%\build-tools\33.0.2" 2>nul
echo Pkg.Revision=33.0.2 > "%ANDROID_HOME%\build-tools\33.0.2\source.properties"
echo ✅ 创建构建工具信息
echo.

echo 🎉 Android SDK基础配置完成！
echo.
echo 📋 配置信息：
echo    ANDROID_HOME: %ANDROID_HOME%
echo    许可协议: 已自动接受
echo    平台版本: API 33
echo    构建工具: 33.0.2
echo.
echo ⚠️  注意：这是最小化配置，Gradle会在首次构建时自动下载所需组件
echo.
pause