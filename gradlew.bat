@echo off
setlocal enabledelayedexpansion

REM 设置默认的JVM选项（增加网络超时配置）
set DEFAULT_JVM_OPTS=-Xmx2048m -Xms256m -Dorg.gradle.daemon.idletimeout=60000 -Dorg.gradle.internal.http.connectionTimeout=120000 -Dorg.gradle.internal.http.socketTimeout=120000

REM 设置Gradle应用目录
set GRADLE_APP_DIR=%~dp0

REM 检查Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到Java环境，请安装Java 11+
    exit /b 1
)

REM 设置Gradle选项
set GRADLE_OPTS=%DEFAULT_JVM_OPTS% -Dorg.gradle.appname=gradlew

REM 运行Gradle
java %GRADLE_OPTS% -cp "%GRADLE_APP_DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
