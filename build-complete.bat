@echo off
chcp 65001 >nul
echo 🐅 小虎学习计划 - 完整自动构建
echo ===================================
echo.

REM 设置变量
set PROJECT_DIR=%cd%
set ANDROID_HOME=%PROJECT_DIR%\android-sdk

REM 步骤1：环境检查
echo 📋 步骤1：环境检查
echo ----------------
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误：未找到Java环境
    echo 📥 请安装Java 11+ 从：https://adoptium.net/
    pause
    exit /b 1
)
echo ✅ Java环境正常
java -version 2>&1 | findstr "version"
echo.

REM 步骤2：SDK配置
echo 📦 步骤2：Android SDK配置
echo -------------------------
if not exist "android-sdk" (
    echo 🔧 配置Android SDK...
    call setup-sdk-minimal.bat
) else (
    echo ✅ Android SDK已存在
)
echo.

REM 步骤3：Gradle配置
echo ⚙️  步骤3：Gradle配置检查
echo -------------------------
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ❌ Gradle wrapper缺失
    echo 📥 请确保gradle-wrapper.jar文件存在
    pause
    exit /b 1
)
echo ✅ Gradle wrapper正常
echo.

REM 步骤4：清理和构建
echo 🔨 步骤4：清理和构建APK
echo -------------------------
echo 🧹 清理之前的构建...
if exist "app\build" rmdir /s /q "app\build" 2>nul
if exist ".gradle" rmdir /s /q ".gradle" 2>nul
echo ✅ 清理完成
echo.

echo 🚀 开始构建APK...
echo ⏰ 预计耗时：3-10分钟（首次构建较慢）
echo.

REM 设置环境变量
set ANDROID_SDK_ROOT=%ANDROID_HOME%
set JAVA_OPTS=-Xmx4096m -XX:MaxMetaspaceSize=1024m

REM 执行构建
call gradlew.bat clean assembleDebug --no-daemon --info
set BUILD_RESULT=%errorlevel%

echo.
echo 📊 构建结果
echo -----------
if %BUILD_RESULT% equ 0 (
    echo ✅ 构建成功！
    echo.
    echo 📱 APK文件位置：
    for %%f in ("app\build\outputs\apk\debug\*.apk") do (
        echo    📦 %%~nxf
        echo       大小：%%~zf 字节
        echo       路径：%%f
    )
    echo.
    echo 🎉 恭喜！小虎学习计划APK构建完成！
    echo 📲 现在可以将APK安装到Android设备上使用了
) else (
    echo ❌ 构建失败（错误代码：%BUILD_RESULT%）
    echo.
    echo 🔍 常见解决方案：
    echo    1. 检查网络连接
    echo    2. 重新运行此脚本
    echo    3. 清理用户Gradle缓存：del /s %USERPROFILE%\.gradle
    echo    4. 检查Java版本是否为11+
)

echo.
echo 按任意键退出...
pause >nul