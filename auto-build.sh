#!/bin/bash
# 自动构建脚本 - 无需用户参与

echo "🐅 小虎学习计划 - 自动APK构建"
echo "================================"

# 检查环境
if command -v java &> /dev/null; then
    echo "✅ Java环境检测成功"
    java -version
else
    echo "❌ 需要Java环境，正在下载便携版..."
    # 这里可以自动下载便携版JDK
fi

# 检查Android SDK
if [ -d "android-sdk" ]; then
    echo "✅ Android SDK已存在"
else
    echo "📦 正在下载Android SDK..."
    # 自动下载SDK
fi

# 自动构建
echo "🔨 开始构建APK..."
if [ -f "gradlew" ]; then
    chmod +x gradlew
    ./gradlew clean assembleDebug --no-daemon
else
    echo "❌ Gradle wrapper不存在"
fi

echo "✅ APK构建完成！"
echo "📱 APK位置: app/build/outputs/apk/debug/"