@echo off
chcp 65001 >nul
title 小虎学习计划 - 一键构建APK

echo.
echo ██████████████████████████████████████
echo █                                    █
echo █   🐅 小虎学习计划 - 一键构建APK     █
echo █                                    █
echo ██████████████████████████████████████
echo.
echo 这个脚本会完全自动化构建过程，您无需任何操作！
echo 请耐心等待 3-10 分钟...
echo.

REM 设置错误处理
setlocal enabledelayedexpansion
set "ERROR_COUNT=0"

REM 步骤1：环境检查
echo [1/6] 🔍 检查Java环境...
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo     ❌ Java环境未找到
    echo     📥 请先安装Java 11+: https://adoptium.net/
    set /a ERROR_COUNT+=1
    goto :END_WITH_ERRORS
)
echo     ✅ Java环境正常
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
    echo     📝 Java版本: %%i
    goto :java_done
)
:java_done
echo.

REM 步骤2：清理环境
echo [2/6] 🧹 清理构建环境...
if exist "app\build" (
    rmdir /s /q "app\build" >nul 2>&1
    echo     ✅ 清理app\build目录
)
if exist ".gradle" (
    rmdir /s /q ".gradle" >nul 2>&1
    echo     ✅ 清理.gradle目录
)
REM 清理用户gradle缓存中可能损坏的文件
if exist "%USERPROFILE%\.gradle\wrapper\dists\gradle-7.5-bin" (
    rmdir /s /q "%USERPROFILE%\.gradle\wrapper\dists\gradle-7.5-bin" >nul 2>&1
    echo     ✅ 清理用户Gradle缓存
)
echo.

REM 步骤3：SDK配置
echo [3/6] 📦 配置Android SDK...
if not exist "android-sdk" (
    echo     🔧 创建Android SDK配置...
    mkdir android-sdk\licenses >nul 2>&1
    mkdir android-sdk\platforms\android-33 >nul 2>&1
    mkdir android-sdk\build-tools\33.0.2 >nul 2>&1
    
    REM 创建许可协议
    echo 24333f8a63b6825ea9c5514f83c2829b004d1fee > android-sdk\licenses\android-sdk-license
    echo 84831b9409646a918e30573bab4c9c91346d8abd > android-sdk\licenses\android-sdk-preview-license
    
    REM 创建最小平台信息
    echo target=android-33 > android-sdk\platforms\android-33\source.properties
    echo pkg.revision=3 >> android-sdk\platforms\android-33\source.properties
    echo AndroidVersion.ApiLevel=33 >> android-sdk\platforms\android-33\source.properties
    
    REM 创建构建工具信息
    echo Pkg.Revision=33.0.2 > android-sdk\build-tools\33.0.2\source.properties
    
    echo     ✅ SDK配置完成
) else (
    echo     ✅ SDK已存在
)
echo.

REM 步骤4：检查Gradle Wrapper
echo [4/6] ⚙️  检查Gradle Wrapper...
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo     ❌ Gradle wrapper文件缺失
    set /a ERROR_COUNT+=1
    goto :END_WITH_ERRORS
)
if not exist "gradlew.bat" (
    echo     ❌ gradlew.bat文件缺失
    set /a ERROR_COUNT+=1
    goto :END_WITH_ERRORS
)
echo     ✅ Gradle Wrapper检查通过
echo.

REM 步骤5：设置环境变量
echo [5/6] 🌍 设置环境变量...
set "ANDROID_HOME=%cd%\android-sdk"
set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
set "JAVA_OPTS=-Xmx4096m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8"
set "SKIP_JDK_VERSION_CHECK=true"
echo     ✅ ANDROID_HOME=%ANDROID_HOME%
echo     ✅ 内存设置: 4GB
echo     ✅ 跳过JDK版本检查（兼容Java 11）
echo.

REM 步骤6：构建APK
echo [6/6] 🚀 构建APK...
echo     ⏰ 首次构建预计需要 5-15 分钟，请耐心等待...
echo     📥 Gradle会自动下载所需的Android SDK组件
echo.

REM 执行构建（带详细输出）
call gradlew.bat clean assembleDebug --no-daemon --info --stacktrace 2>&1
set "BUILD_RESULT=!errorlevel!"

echo.
echo ==========================================
if !BUILD_RESULT! equ 0 (
    echo 🎉 构建成功！
    echo.
    echo 📱 APK文件信息：
    for %%f in ("app\build\outputs\apk\debug\*.apk") do (
        echo     📦 文件名：%%~nxf
        echo     📏 大小：%%~zf 字节
        echo     📍 完整路径：%%f
        echo.
    )
    echo ✅ 小虎学习计划APK构建完成！
    echo 📲 现在可以将APK安装到Android设备上使用了
    echo.
    echo 🔧 安装步骤：
    echo     1. 将APK文件传输到Android设备
    echo     2. 在设备上启用"未知来源安装"
    echo     3. 点击APK文件进行安装
    echo     4. 首次运行时授予必要权限
    echo.
) else (
    echo ❌ 构建失败 (错误代码: !BUILD_RESULT!)
    echo.
    echo 🔍 请尝试以下解决方案：
    echo     1. 检查网络连接是否正常
    echo     2. 重新运行此脚本（第二次通常会更快）
    echo     3. 确保有足够的磁盘空间（至少2GB）
    echo     4. 关闭防火墙/杀毒软件后重试
    echo     5. 使用管理员权限运行此脚本
    set /a ERROR_COUNT+=1
)
goto :END

:END_WITH_ERRORS
echo.
echo ==========================================
echo ❌ 发现 !ERROR_COUNT! 个错误，构建失败
echo.
echo 📋 请解决以上问题后重新运行此脚本
echo.

:END
echo ==========================================
echo 脚本执行完毕
echo.
echo 按任意键退出...
pause >nul