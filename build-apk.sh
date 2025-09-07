#!/bin/bash
echo "Starting APK build process..."

# 设置环境变量
export ANDROID_HOME=/usr/local/lib/android/sdk
echo "ANDROID_HOME: $ANDROID_HOME"

# 接受SDK许可
echo "Accepting Android SDK licenses..."
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

# 设置local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 给gradlew执行权限
chmod +x gradlew

# 清理项目
echo "Cleaning project..."
./gradlew clean

# 构建APK
echo "Building APK..."
./gradlew :app:assembleDebug --stacktrace

# 检查APK是否生成成功
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
  echo "APK build successful!"
  ls -la app/build/outputs/apk/debug/
else
  echo "APK build failed!"
  ls -la app/build/outputs/apk/debug/
  exit 1
fi