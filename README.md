# AIpet

一个可扩展的 Android 虚拟宠物应用，支持多页面互动、AI 聊天、多 Provider 网络路由、设置中心与日志可观测能力。

## 项目亮点

- 首页聚合核心能力：宠物展示、时间天气、快捷入口、最近聊天。
- 聊天链路支持多 Provider：OpenAI、豆包、本地与自定义服务。
- 设置中心统一管理：API 配置、模型参数、图片处理、日志查看、角色卡入口。
- 业务闭环完整：喂食、商店、外出、好感、帮助、换装可从主页直达并回传结果。
- 网络架构清晰：Facade + Router + RequestExecutor，便于维护与扩展。
- 日志体系完善：支持请求、响应、错误、降级行为追踪。

## 技术栈

- Android (Java)
- Gradle Kotlin DSL
- Retrofit + OkHttp
- SharedPreferences 本地持久化

## 目录结构

```text
AIpet/
├─ app/
│  ├─ src/main/java/com/example/aipet/
│  │  ├─ network/            # 网络层（ApiClient、Router、请求执行）
│  │  ├─ ui/activity/        # 页面 Activity
│  │  ├─ ui/navigation/      # 统一导航
│  │  ├─ ui/avatar/          # 头像处理管线
│  │  └─ util/               # 日志、存储与通用工具
│  ├─ src/main/res/          # 资源文件（home_* 分层）
│  └─ Planning/              # 模块设计与阶段文档
├─ test-backend/             # 本地后端调试服务
├─ gradlew / gradlew.bat
└─ build.gradle.kts
```

## 快速开始

### 1) 环境要求

- Android Studio（建议最新稳定版）
- JDK 17（与 Android Gradle Plugin 兼容）
- Android SDK（根据 app/build.gradle.kts 配置安装）

### 2) 克隆并打开工程

在 Android Studio 中打开项目根目录 AIpet。

### 3) 构建与运行

Windows:

```bash
.\gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

如需直接安装到设备：

```bash
./gradlew installDebug
```

## 配置说明

首次运行建议在设置页完成以下配置：

- Provider 选择（OpenAI / 豆包 / Local / Custom）
- API Key、Base URL、Model
- 图片处理开关与端点（下载、去背、上传）

配置保存后会同步到运行时网络配置，聊天可立即生效。

## 本地后端联调（可选）

项目提供 test-backend 目录用于本地接口调试。

1. 进入 test-backend 目录。
2. 安装依赖：

```bash
pip install -r requirements.txt
```

3. 启动服务：

```bash
python local_api_server.py
```

或使用脚本：

```bash
run_server.bat
```

## 核心模块说明

- MainActivity：主页主控，协调时间天气、入口导航、聊天发送与回传处理。
- ApiClient：网络总入口，负责配置校验、服务初始化与请求派发。
- NetworkChatRouter：按 Provider 路由到不同请求实现。
- AssistantReplyFormatter：将 AI 返回拆分为可展示 answer 与日志 thinking。
- SettingsActivity + ApiSettingsStore：设置保存与持久化。
- AvatarImagePipeline：头像下载、去背、上传、保存流程。
- ChatLogger：链路日志记录与问题定位支撑。

## 常见开发任务

### 执行测试

```bash
./gradlew test
```

### 检查构建问题

```bash
./gradlew build
```

### 清理构建缓存

```bash
./gradlew clean
```

## 建议迭代方向

- 补强头像去背/上传接口的响应体校验与重试策略。
- 为聊天发送、设置保存、活动回传补齐自动化测试。
- 增加主页启动关键控件的空指针防护与埋点。

## 参考文档

- app/Planning/2026-04-13-codebase-main-summary.md
- Planning/QUICK_START_LOCAL_TEST.md
- Planning/PROJECT_SUMMARY.md

## 许可证

当前仓库未声明开源许可证。若计划公开发布，请补充 LICENSE 文件并明确使用条款。
