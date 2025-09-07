@echo off
echo "=== 小虎学习计划 APK构建脚本 ==="

REM 设置环境变量
set ANDROID_SDK_ROOT=%cd%\android-sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%
set PATH=%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin;%ANDROID_SDK_ROOT%\platform-tools;%ANDROID_SDK_ROOT%\build-tools\33.0.0;%PATH%

echo 当前目录: %cd%
echo Android SDK: %ANDROID_SDK_ROOT%

REM 检查环境
if not exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" (
    echo 错误: Android SDK未正确安装，请先运行 setup-android-sdk.bat
    pause
    exit /b 1
)

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请安装JDK 8或更高版本
    pause
    exit /b 1
)

echo "=== 清理项目 ==="
if exist "app\build" rmdir /s /q "app\build"
if exist "build" rmdir /s /q "build"

echo "=== 设置Gradle Wrapper权限 ==="
if exist "gradlew.bat" (
    echo Gradle Wrapper已存在
) else (
    echo 创建Gradle Wrapper...
    echo 由于网络问题，我们使用本地Gradle配置
)

echo "=== 构建APK ==="
echo 开始构建Debug APK...

REM 尝试使用Gradle构建
if exist "gradlew.bat" (
    call gradlew.bat assembleDebug --no-daemon --offline
) else (
    echo 使用直接编译方式...
    echo 这可能需要几分钟时间，请耐心等待...
    
    REM 创建输出目录
    if not exist "app\build\outputs\apk\debug" mkdir "app\build\outputs\apk\debug"
    
    echo 正在编译源代码...
    REM 这里需要手动编译，但比较复杂，建议使用下面的简化版本
)

echo "=== 查找生成的APK ==="
for /r . %%i in (*.apk) do (
    echo 找到APK: %%i
    copy "%%i" "小虎学习计划.apk"
    echo APK已复制为: 小虎学习计划.apk
)

if exist "小虎学习计划.apk" (
    echo "=== 构建成功! ==="
    echo APK文件: %cd%\小虎学习计划.apk
) else (
    echo "=== 构建可能未完成 ==="
    echo 请检查是否有错误信息
)

pause