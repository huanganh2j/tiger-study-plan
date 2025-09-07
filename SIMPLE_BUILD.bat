@echo off
setlocal enabledelayedexpansion

REM 简化版构建脚本 - 直接使用本地工具
echo 🐅 小虎学习计划 - 简化构建
echo ==============================

REM 检查Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 需要Java环境
    pause
    exit /b 1
)
echo ✅ Java环境正常

REM 设置环境变量
set "ANDROID_HOME=%cd%\android-sdk"
set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
set "SKIP_JDK_VERSION_CHECK=true"

echo ✅ 环境变量已设置

REM 清理构建目录
if exist "app\build" rmdir /s /q "app\build" 2>nul
if exist ".gradle" rmdir /s /q ".gradle" 2>nul

echo ✅ 清理完成

REM 直接使用Java执行gradle任务
echo 🚀 开始构建APK...

REM 创建一个简单的gradle执行器
java -cp "gradle\wrapper\gradle-wrapper.jar" -Dorg.gradle.appname=gradlew org.gradle.wrapper.GradleWrapperMain clean assembleDebug --no-daemon --offline
set BUILD_RESULT=%errorlevel%

if %BUILD_RESULT% equ 0 (
    echo ✅ 构建成功！
    echo 📱 APK位置: app\build\outputs\apk\debug\
    for %%f in ("app\build\outputs\apk\debug\*.apk") do (
        echo    📦 %%~nxf
    )
) else (
    echo ❌ 构建失败 (错误代码: %BUILD_RESULT%)
    echo 正在尝试在线模式...
    java -cp "gradle\wrapper\gradle-wrapper.jar" -Dorg.gradle.appname=gradlew org.gradle.wrapper.GradleWrapperMain clean assembleDebug --no-daemon
)

pause