@echo off
chcp 65001 >nul
REM 小虎学习计划 - Windows自动构建脚本
echo 🐅 小虎学习计划 - 自动APK构建
echo ================================
echo.

REM 检查当前目录
echo 📁 检查项目目录...
if not exist "app\build.gradle" (
    echo ❌ 错误: 找不到Android项目文件
    echo 请确认在正确的项目目录下运行此脚本
    pause
    exit /b 1
)
echo ✅ 项目目录检查通过
echo.

REM 检查Java环境
echo ☕ 检查Java环境...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java环境检测成功
    java -version
) else (
    echo ❌ 需要Java环境
    echo 请安装Java 11或更高版本
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)
echo.

REM 检查Gradle Wrapper
echo 🔧 检查Gradle Wrapper...
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ❌ Gradle wrapper不存在
    echo 正在下载Gradle wrapper...
    REM 这里可以添加下载逻辑
    echo 请手动下载Gradle 7.5
    pause
    exit /b 1
)
echo ✅ Gradle wrapper存在
echo.

REM 检查Android SDK
echo 📦 检查Android SDK...
if exist "android-sdk" (
    echo ✅ Android SDK目录存在
) else (
    echo ⚠️ Android SDK不存在，将使用自动下载
)
echo.

REM 清理之前的构建
echo 🧹 清理之前的构建...
if exist "app\build" (
    rmdir /s /q "app\build" 2>nul
)
if exist ".gradle" (
    rmdir /s /q ".gradle" 2>nul
)
echo ✅ 清理完成
echo.

REM 构建APK
echo 🔨 开始构建APK...
echo 这可能需要3-10分钟，请耐心等待...
echo.

if exist "gradlew.bat" (
    echo 正在执行: gradlew.bat clean assembleDebug
    call gradlew.bat clean assembleDebug --no-daemon --stacktrace
    set BUILD_RESULT=%errorlevel%
    
    if !BUILD_RESULT! equ 0 (
        echo.
        echo ✅ APK构建成功！
        echo 📱 APK位置: app\build\outputs\apk\debug\
        echo.
        echo 构建的APK文件：
        if exist "app\build\outputs\apk\debug\*.apk" (
            for %%f in ("app\build\outputs\apk\debug\*.apk") do (
                echo    - %%~nxf (%%~zf 字节)
            )
        ) else (
            echo    没有找到APK文件
        )
        echo.
        echo 🎉 构建完成！您可以安装并使用小虎学习计划了！
    ) else (
        echo.
        echo ❌ 构建失败 (错误代码: !BUILD_RESULT!)
        echo.
        echo 可能的解决方案：
        echo 1. 检查网络连接，确保可以下载依赖
        echo 2. 再次运行此脚本
        echo 3. 检查Java版本是否为11+
        echo 4. 清理C:\Users\%USERNAME%\.gradle目录后重试
    )
) else (
    echo ❌ Gradle wrapper不存在
    echo 请确认项目结构是否完整
)

echo.
echo 按任意键退出...
pause >nul