# 📱 AI 宠物助手 - Android 项目框架文档

## 项目概览
本项目是一个基于 **Activity + XML** 架构的 Android 移动端应用框架，包含 5 个核心页面和完整的项目配置。

---

## 📁 项目目录结构

```
E:/Work/AIpet/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/aipet/
│   │   │   │   ├── ui/
│   │   │   │   │   └── activity/
│   │   │   │   │       ├── SplashActivity.java          ✨ 启动页
│   │   │   │   │       ├── MainActivity.java            🏠 首页
│   │   │   │   │       ├── CreatePetActivity.java       ➕ 创建角色页
│   │   │   │   │       ├── PetCardListActivity.java     📋 角色卡列表页
│   │   │   │   │       └── ChatActivity.java            💬 聊天页
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/                          (后续数据模型)
│   │   │   │   │   └── local/                          (本地存储)
│   │   │   │   └── util/                               (工具类)
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_splash.xml             启动页布局
│   │   │   │   │   ├── activity_main.xml               首页布局
│   │   │   │   │   ├── activity_create_pet.xml         创建角色布局
│   │   │   │   │   ├── activity_pet_card_list.xml      角色列表布局
│   │   │   │   │   └── activity_chat.xml               聊天页布局
│   │   │   │   │
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml                     字符串资源
│   │   │   │   │   ├── colors.xml                      颜色资源
│   │   │   │   │   ├── dimens.xml                      尺寸资源 ⭐ 新增
│   │   │   │   │   ├── themes.xml                      主题配置
│   │   │   │   │   └── values-night/themes.xml         深色主题
│   │   │   │   │
│   │   │   │   ├── drawable/                           图片资源
│   │   │   │   └── mipmap-*/                           应用图标
│   │   │   │
│   │   │   └── AndroidManifest.xml                    ⭐ 已更新 - 注册 5 个 Activity
│   │   │
│   │   └── test/androidTest/                           单元测试
│   │
│   └── build.gradle.kts                              ⭐ 已更新 - 添加依赖注释
│
├── build.gradle.kts                                   项目级配置
├── settings.gradle.kts                               工程配置
└── gradle/                                            Gradle 包装器
```

---

## 🔧 关键配置文件

### ✅ AndroidManifest.xml
已注册 5 个 Activity：

```xml
<!-- 启动页 - 应用入口 -->
<activity
    android:name=".ui.activity.SplashActivity"
    android:exported="true"
    android:theme="@style/Theme.AIpet.NoActionBar">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- 首页 -->
<activity
    android:name=".ui.activity.MainActivity"
    android:exported="true" />

<!-- 创建角色页 -->
<activity
    android:name=".ui.activity.CreatePetActivity"
    android:exported="false" />

<!-- 角色卡列表页 -->
<activity
    android:name=".ui.activity.PetCardListActivity"
    android:exported="false" />

<!-- 聊天页 -->
<activity
    android:name=".ui.activity.ChatActivity"
    android:exported="false" />
```

---

## 📦 基础依赖配置

### 当前依赖（build.gradle.kts）

```gradle
dependencies {
    // ✅ 已包含 - Jetpack 基础库
    implementation(libs.appcompat)              // AppCompat 向后兼容
    implementation(libs.material)               // Material Design 3
    implementation(libs.activity)               // Activity 基类
    implementation(libs.constraintlayout)       // 约束布局

    // ❌ 可选注释 - 按需取消注释启用
    // implementation("androidx.room:room-runtime:2.6.1")
    // annotationProcessor("androidx.room:room-compiler:2.6.1")

    // implementation("androidx.datastore:datastore-preferences:1.0.0")

    // implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    // implementation("io.reactivex.rxjava3:rxandroid:3.0.0")

    // implementation("com.squareup.retrofit2:retrofit:2.10.0")
    // implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    // implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // implementation("com.google.code.gson:gson:2.10.1")

    // ✅ 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

### 推荐后续添加的依赖

| 库名称 | 用途 | 版本 | 是否必需 |
|--------|------|------|----------|
| **Room** | 本地 SQLite 数据库 | 2.6.1 | 当需要本地存储 |
| **DataStore** | SharedPreferences 替代品 | 1.0.0 | 当需要键值对存储 |
| **Retrofit** | HTTP 网络请求 | 2.10.0 | 当需要网络通信 |
| **OkHttp** | HTTP 客户端 | 4.11.0 | 当使用 Retrofit |
| **Gson** | JSON 解析 | 2.10.1 | 当需要 JSON 转换 |
| **RxJava** | 响应式编程 | 3.1.8 | 当需要异步处理 |
| **Hilt** | 依赖注入 | 2.50 | 当需要 DI 框架 |

---

## 🎯 页面设计

### 1. **SplashActivity** - 启动页
- **功能**: 欢迎屏幕，延迟 2 秒后自动跳转到首页
- **布局**: `activity_splash.xml` - 简洁的欢迎文本
- **流程**: `SplashActivity` → `MainActivity`

### 2. **MainActivity** - 首页
- **功能**: 应用主菜单，提供 3 个导航按钮
- **按钮**:
  - ➕ 创建新角色 → `CreatePetActivity`
  - 📋 查看我的角色 → `PetCardListActivity`
  - 💬 开始聊天 → `ChatActivity`

### 3. **CreatePetActivity** - 创建角色页
- **功能**: 让用户输入角色信息（名称、描述）并创建
- **表单字段**:
  - 角色名称 (EditText)
  - 角色描述 (多行 EditText)
- **操作**: 创建 / 取消

### 4. **PetCardListActivity** - 角色卡列表页
- **功能**: 显示已创建的所有角色卡片
- **展示**: ListView 列表 + 空状态提示

### 5. **ChatActivity** - 聊天页
- **功能**: 与选定角色进行对话交互
- **组件**:
  - 消息列表 (ListView)
  - 消息输入框 (EditText)
  - 发送按钮 (Button)

---

## 🚀 下一步开发指南

### 1️⃣ 本地数据存储
```bash
# 取消注释 build.gradle.kts 中的 Room 依赖
implementation("androidx.room:room-runtime:2.6.1")
annotationProcessor("androidx.room:room-compiler:2.6.1")

# 创建 data/model/ 包
# 定义 Pet.java 实体类
# 创建 PetDao 数据访问接口
# 创建 AppDatabase 数据库类
```

### 2️⃣ 网络通信（AI 接口）
```bash
# 取消注释 build.gradle.kts 中的 Retrofit 依赖
implementation("com.squareup.retrofit2:retrofit:2.10.0")
implementation("com.squareup.retrofit2:converter-gson:2.10.0")

# 创建 data/remote/ 包
# 定义 API 服务接口（如 ChatApiService）
# 实现网络请求和数据转换
```

### 3️⃣ 消息适配器
```bash
# 创建 ui/adapter/ 包
# 实现 ChatMessageAdapter 自定义列表适配器
# 显示用户和 AI 的消息气泡
```

### 4️⃣ ViewModel 和 LiveData（推荐）
```bash
# 创建 ui/viewmodel/ 包
# 为各 Activity 创建对应的 ViewModel
# 实现数据和 UI 的分离
```

---

## 📝 编译和运行

### 环境要求
- **Android Studio**: 最新版本（2024.2 或更高）
- **Android SDK**: API 33+（minSdk = 33）
- **Java**: JDK 11+
- **Gradle**: 8.0+（由 Android Studio 管理）

### 编译步骤
```bash
# 1. 同步 Gradle（自动或手动）
./gradlew build

# 2. 在模拟器或真机上运行
./gradlew installDebug

# 3. 或者通过 Android Studio 运行
# 菜单: Run > Run 'app' 或按 Shift+F10
```

---

## 🎨 项目配置总结

| 配置项 | 值 | 备注 |
|--------|-----|------|
| 应用包名 | `com.example.aipet` | 生产环境请修改 |
| 最小 SDK | 33 | Android 13+ |
| 目标 SDK | 36 | Android 15 |
| 编译 SDK | 36 | Android 15 |
| Java 版本 | 11 | 支持 Lambda 表达式 |
| 主题 | Material Design 3 | 亮色/深色模式 |

---

## ✨ 特性检查清单

- ✅ 5 个核心 Activity 已实现
- ✅ 对应的 5 个 XML 布局已创建
- ✅ AndroidManifest.xml 已配置
- ✅ 字符串资源 (strings.xml) 已完善
- ✅ 颜色资源 (colors.xml) 已定义
- ✅ 尺寸资源 (dimens.xml) 已创建
- ✅ build.gradle.kts 已配置及文档化
- ✅ 项目结构符合 Android 最佳实践
- ⏳ 后续可添加：数据库、网络、适配器、ViewModel

---

## 💡 Java 最佳实践提示

1. **包命名规范**: `com.example.aipet.ui.activity` / `data.model` / `util`
2. **类命名规范**: `PascalCase` (如 `CreatePetActivity`)
3. **方法命名规范**: `camelCase` (如 `initViews()`)
4. **常量命名规范**: `UPPER_SNAKE_CASE` (如 `SPLASH_DELAY`)
5. **资源文件命名**: 全小写 + 下划线 (如 `activity_create_pet.xml`)
6. **注释**: 使用 JavaDoc 风格注释关键类和方法
7. **异常处理**: 总是处理可能的 null 和异常
8. **内存泄漏**: 避免匿名类持有 Activity 引用，使用弱引用

---

## 🔗 相关资源

- [Android 官方文档](https://developer.android.com/)
- [Jetpack 库指南](https://developer.android.com/jetpack)
- [Material Design 3](https://m3.material.io/)
- [Android 最佳实践](https://developer.android.com/guide/practices)

---

**项目框架生成时间**: 2026-03-31
**Java 版本**: 11+
**目标 API**: 33-36
