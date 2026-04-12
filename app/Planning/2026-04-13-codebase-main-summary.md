# AIpet 代码主总结（总分结构）

## 一、总体总结

当前工程已经从“单页面演示”演进为“可扩展的多页面宠物应用”：

1. 首页承载宠物展位、天气时间、快捷功能入口、最近聊天面板与输入发送能力。
2. 聊天链路支持多 Provider（OpenAI、豆包、本地、自定义），并将“回答内容”和“深度思考”分离处理。
3. 设置页成为统一配置中枢，覆盖 API 配置、图片处理、角色卡管理、日志查看，并采用折叠分组降低复杂度。
4. 业务页（商店、喂食、外出、好感、帮助、换装）已形成完整导航闭环，支持从主页触达并回传结果。
5. 工具层采用“总入口 + 分布式子模块”模式，提升可维护性与复用性。

整体设计方向是：主页聚合、能力下沉、配置集中、链路可观测。

---

## 二、分模块讲解与实现方法

## 2.1 启动与页面导航

### 作用说明
应用从启动页进入主页，主页再分发到各功能页。导航意图被统一抽象，避免 Activity 中硬编码跳转。

### 实现方法
1. 在 AndroidManifest 注册各 Activity，启动入口为 SplashActivity。
2. 使用 UiNavigator 统一构建 Intent（toMain、toSettings、toHelp、toFeed、toStore 等）。
3. 各页面通过 navigateTo 调用，减少跨页面耦合。

核心文件：
- src/main/AndroidManifest.xml
- src/main/java/com/example/aipet/ui/navigation/UiNavigator.java
- src/main/java/com/example/aipet/ui/activity/SplashActivity.java

---

## 2.2 首页主控（MainActivity）

### 作用说明
MainActivity 是主交互枢纽，负责：
1. 时间日期刷新与天气定位。
2. 宠物展位显示与切换。
3. 最近聊天面板与顶部气泡。
4. 五大功能入口（帮助、喂食、商店、外出、设置）。
5. 聊天输入与发送动作。

### 实现方法
1. bindViews 统一绑定首页关键控件。
2. clockHandler + clockRunnable 周期刷新时间与天气。
3. ActivityResultLauncher 分别接收喂食、商店、外出页面回传。
4. sendChatMessage 执行输入校验、调用网络层、更新日志和好感值。
5. 通过 recentChats 列表驱动最近聊天渲染，并支持全屏查看。

核心文件：
- src/main/java/com/example/aipet/ui/activity/MainActivity.java
- src/main/res/layout/activity_main.xml

---

## 2.3 聊天与响应格式化链路

### 作用说明
聊天页与首页都复用统一 AI 返回处理逻辑，核心目标是“显示可读回答、保留深度思考日志”。

### 实现方法
1. 发送前构造 PetInfo 与系统提示词（PetPromptBuilder）。
2. API 返回后交给 APIAnswer.fromRaw 解析。
3. AssistantReplyFormatter 负责从复杂响应文本中提取 answer 和 thinking。
4. UI 仅显示 answerOnly，thinking 进入日志块。

核心文件：
- src/main/java/com/example/aipet/ui/activity/ChatActivity.java
- src/main/java/com/example/aipet/network/APIAnswer.java
- src/main/java/com/example/aipet/network/AssistantReplyFormatter.java
- src/main/java/com/example/aipet/network/PetPromptBuilder.java

---

## 2.4 网络架构（Facade + Router）

### 作用说明
网络层采用“外观 + 路由”模式：
- ApiClient 负责统一入口、Retrofit/OkHttp 生命周期、配置校验。
- NetworkChatRouter 负责按 Provider 分发到对应请求路径。

### 实现方法
1. ApiClient.getApiService 延迟初始化 Retrofit，并统一超时、重试、日志拦截器。
2. ApiClient.sendChatMessage 先进行输入与配置校验，再委托给 NetworkChatRouter。
3. Router 根据 Provider 分发：
   - OpenAI：OpenAIChatRequest
   - Doubao：DoubaoApiClient
   - Local/Custom：ChatRequest
4. NetworkRequestExecutor 统一处理响应和错误格式。

核心文件：
- src/main/java/com/example/aipet/network/ApiClient.java
- src/main/java/com/example/aipet/network/NetworkChatRouter.java
- src/main/java/com/example/aipet/network/request/NetworkRequestExecutor.java
- src/main/java/com/example/aipet/network/DoubaoApiClient.java

---

## 2.5 设置中心与头像处理管线

### 作用说明
设置页已经成为系统配置中枢：
1. API 提供商与模型配置。
2. 图片 URL、去背端点、上传端点及自动处理开关。
3. 角色卡入口、日志入口、帮助页与换装页入口。

### 实现方法
1. SettingsActivity 将页面划分为四个折叠区（角色、图片、API、日志）。
2. ApiSettingsStore 负责持久化 provider/api/model 与图片处理参数。
3. AvatarImagePipeline 按顺序执行：下载图片 -> 可选去背 -> 可选上传 -> 本地保存。
4. 设置保存后同步到 ApiConfig/ApiClient，保证聊天立即生效。

核心文件：
- src/main/java/com/example/aipet/ui/activity/SettingsActivity.java
- src/main/java/com/example/aipet/util/store/ApiSettingsStore.java
- src/main/java/com/example/aipet/ui/avatar/AvatarImagePipeline.java

---

## 2.6 宠物数据与业务联动（喂食/商店/外出/好感）

### 作用说明
宠物状态围绕“好感度 + 外观 + 历史记录”进行联动。

### 实现方法
1. PetStore 负责宠物列表增删改查，基于 SPUtils 序列化存储。
2. 商店页回传服饰信息，主页接收后解锁/应用服饰并增加好感。
3. 喂食页与外出页回传增益值，主页统一更新好感并写入历史。
4. AffectionHistoryStore 记录行为来源与增益，供好感页展示。

核心文件：
- src/main/java/com/example/aipet/util/store/PetStore.java
- src/main/java/com/example/aipet/ui/activity/StoreActivity.java
- src/main/java/com/example/aipet/ui/activity/FeedActivity.java
- src/main/java/com/example/aipet/ui/activity/OutingActivity.java
- src/main/java/com/example/aipet/util/AffectionHistoryStore.java

---

## 2.7 日志与可观测性

### 作用说明
日志系统支持开发排错与用户问题回溯，覆盖请求、响应、错误、降级行为。

### 实现方法
1. ChatLogger 同步写入内存和文件，提供按等级/标签筛选能力。
2. 业务处记录关键链路：用户消息、API 请求、API 回答块、异常、fallback。
3. ChatLogViewerActivity 和 ErrorLogViewerActivity 提供可视化查看入口。

核心文件：
- src/main/java/com/example/aipet/util/ChatLogger.java
- src/main/java/com/example/aipet/ui/activity/ChatLogViewerActivity.java
- src/main/java/com/example/aipet/ui/activity/ErrorLogViewerActivity.java

---

## 2.8 资源分层与 UI 组织

### 作用说明
资源命名已采用首页前缀化与分层治理，减少通用资源污染并便于替换素材。

### 实现方法
1. 首页资源集中在 home_strings/home_colors/home_styles/home_dimens。
2. drawable 采用 bg_home_ 与 ic_home_ 前缀进行逻辑分组。
3. activity_main 通过 home_* 资源驱动布局参数，降低硬编码。

核心文件：
- src/main/res/values/home_strings.xml
- src/main/res/values/home_colors.xml
- src/main/res/values/home_styles.xml
- src/main/res/values/home_dimens.xml
- src/main/res/layout/activity_main.xml

---

## 三、当前完成度与后续建议

## 3.1 当前完成度
1. 多页面业务流程闭环已形成。
2. 网络多 Provider 路由与配置同步已落地。
3. 首页与设置页已具备可持续演进的模块边界。
4. 日志体系可支撑日常联调和故障定位。

## 3.2 建议优先推进项
1. 增加 Gradle Wrapper，统一构建环境，避免本地 gradle 不一致导致编译失败。
2. 为头像去背/上传接口补充响应体校验与重试策略，提升真实服务稳定性。
3. 为 MainActivity 增加启动期关键控件空指针防护和埋点，降低回归风险。
4. 为核心流程补齐自动化测试：设置保存、聊天发送、活动回传、头像处理。

---

## 四、实现策略总结（可复用方法论）

1. 总入口模式：以 ApiClient、UtilHub、UiNavigator 作为总引导，避免能力散落。
2. 分布式实现：把 provider 路由、存储、请求执行拆到独立模块，降低改动影响面。
3. 先可用再增强：先打通页面与链路，再逐步加折叠、日志、兜底、自适应。
4. 面向回归修复：每次结构调整后优先跑错误检查并修复启动链路问题。

该总结可作为下一轮迭代的主参考文档。