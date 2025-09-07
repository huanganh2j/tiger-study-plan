# 小虎学习计划 - 自动构建脚本

## 功能说明
这是一个Android语音学习计划管理应用，包含以下核心功能：
- 语音新增学习计划
- 定时提醒（开始前1分钟 + 结束时）
- 完成状态确认
- 语音修改/删除计划
- 重复计划功能
- 历史记录查看
- Excel导出功能
- 华为手机权限优化

## 技术栈
- 语言：Kotlin
- 架构：MVVM
- 数据库：Room (SQLite)
- 构建工具：Gradle 7.4.2
- 目标平台：Android API 33

## 构建说明
本项目已配置GitHub Actions自动构建，推送代码后会自动生成APK文件。

项目结构：
- app/ - 主应用代码
- .github/workflows/build-apk.yml - 自动构建配置
- build.gradle - 构建配置
- gradle.properties - Gradle配置

## 安装说明
1. 下载生成的APK文件
2. 在Android设备上启用"未知来源安装"
3. 安装APK
4. 首次运行时授予录音、存储等权限
5. 华为设备需额外设置"允许后台运行"和"忽略电池优化"

### 📱 应用功能
- ✅ 语音新增学习计划
- ✅ 定时提醒（开始前1分钟 + 结束时）
- ✅ 完成状态确认
- ✅ 语音修改/删除计划
- ✅ 重复计划（当天/学习日/每天）
- ✅ 历史记录查看（按日期分组）
- ✅ Excel导出功能
- ✅ 华为手机权限引导
- ✅ 锁屏状态提醒

### 🏗️ 构建APK方法

#### 方法1：使用Android Studio
1. 打开Android Studio
2. 选择 "Open an existing project"
3. 选择 `e:\qoder` 目录
4. 等待项目同步完成
5. 点击 Build → Build Bundle(s) / APK(s) → Build APK(s)
6. APK文件将生成在 `app/build/outputs/apk/debug/` 目录

#### 方法2：命令行构建（需要Android SDK）
```bash
# 确保ANDROID_HOME环境变量已设置
cd e:\qoder
gradle assembleDebug
```

### 📂 项目结构
```
e:\qoder/
├── app/
│   ├── build.gradle           # 应用配置
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/studyplan/tiger/
│       │   ├── MainActivity.kt
│       │   ├── HistoryActivity.kt
│       │   ├── service/
│       │   ├── utils/
│       │   ├── data/
│       │   └── ...
│       └── res/               # 资源文件
├── build.gradle              # 项目配置
├── settings.gradle           # 项目设置
└── gradle/                   # Gradle wrapper
```

### 🚀 安装说明
1. 构建完成后，将APK文件传输到华为荣耀V20
2. 开启"未知来源应用安装"权限
3. 安装APK
4. 首次运行时按提示设置权限（录音、存储、后台运行、忽略电池优化）
5. 开始使用语音添加学习计划！

### 🎯 使用示例
1. 点击语音按钮
2. 说："口算，下午4点45到下午5点"
3. 选择重复方式："学习日"
4. 等待自动提醒！

**项目已100%完成，所有需求功能都已实现！** 🎉