# 📱 API 设置界面 - 快速指南

## 功能概览

**SettingsActivity** 提供了一个完整的 API 配置界面，用户可以轻松配置和管理 AI 服务的 API 连接。

### 🎯 支持的 API 提供商

#### 1️⃣ **OpenAI (GPT-3.5/GPT-4)**
- **服务**: ChatGPT 官方 API
- **URL**: `https://api.openai.com/v1`
- **获取 API Key**: 访问 [platform.openai.com](https://platform.openai.com)
- **费用**: 按使用量计费

#### 2️⃣ **本地后端**
- **服务**: 自建本地服务
- **URL**: `http://localhost:8080`
- **优势**: 无需外网，快速测试，成本低
- **要求**: 后端服务必须在指定地址运行

#### 3️⃣ **自定义 API**
- **灵活配置**: 任何兼容 OpenAI 的 API
- **示例**: Azure OpenAI、国内云厂商等
- **需要**: API URL 和认证密钥

---

## 🚀 使用步骤

### 第 1 步：打开应用

1. 运行 AIpet 应用
2. 在主菜单点击 **⚙️ API 设置** 按钮
3. 进入设置页面

### 第 2 步：选择 API 提供方

在 **选择 API 提供方** 部分选择你要使用的服务：

```
○ OpenAI (GPT-3.5/GPT-4)    ← 推荐生产环境
○ 本地后端                   ← 推荐开发测试
○ 自定义 API                 ← 高级用户
```

### 第 3 步：填入 API 信息

#### **OpenAI 配置示例**

```
API 端点 URL:  https://api.openai.com/v1
API 密钥:      sk-proj-xxxxxxxxxxxxxxxxxxxx
模型名称:      gpt-3.5-turbo
```

#### **本地后端配置示例**

```
API 端点 URL:  http://localhost:8080
API 密钥:      (留空 - 本地无需认证)
模型名称:      local
```

#### **自定义 API 配置示例**

```
API 端点 URL:  https://your-api.com/v1
API 密钥:      your-secret-key
模型名称:      your-model
```

### 第 4 步：保存和测试

#### 💾 保存设置
- 点击 **💾 保存设置** 按钮
- 配置将保存到本地 SharedPreferences
- 下次打开应用自动加载

#### 🔗 测试连接
- 点击 **🔗 测试连接** 按钮
- 系统将验证 URL 格式
- 返回状态提示

#### 🔄 重置默认
- 点击 **🔄 重置默认** 按钮
- 恢复到初始配置
- 清除所有自定义设置

---

## 📊 UI 界面详解

### 状态指示器

| 状态 | 颜色 | 说明 |
|------|------|------|
| ✓ 设置已保存 | 🟢 绿色 | 配置成功保存 |
| ✓ 连接成功 | 🟢 绿色 | API 连接验证通过 |
| ⏳ 正在测试连接... | 🔵 蓝色 | 测试进行中 |
| ✗ 连接失败 | 🔴 红色 | 连接失败需要调整 |
| 状态：未配置 | 🟠 橙色 | 初始状态 |

### 输入框验证

```
字段          | 必填 | 示例
-------------|------|----
API 端点 URL | 是   | https://api.openai.com/v1
API 密钥     | 是*  | sk-...
             |      | (*本地后端可选)
模型名称     | 是   | gpt-3.5-turbo
```

---

## 🔐 安全建议

### 🛡️ API Key 保护

1. **不要硬编码**: 不在代码中写入 API Key
2. **加密存储**: 敏感信息使用加密保存
3. **定期更换**: 每月更换一次 API Key
4. **环境变量**: 使用 BuildConfig 或环境变量

### ⚠️ 隐私考虑

- API 密钥输入框使用密码类型隐藏输入
- 数据仅在本地设备存储
- 不会上传到任何服务器

---

## 🔧 高级配置

### 程序员配置方式

也可以在代码中直接配置（不推荐用于生产环境）:

```java
// 配置 OpenAI
ApiConfig.getInstance().configureOpenAI(
    "sk-...",           // API Key
    "gpt-3.5-turbo"     // 模型
);

// 配置本地后端
ApiConfig.getInstance().configureLocalBackend(
    "http://localhost:8080"
);

// 配置自定义 API
ApiConfig.getInstance().configureCustom(
    "https://your-api.com/v1",
    "your-api-key",
    "your-model"
);
```

### 从代码读取配置

```java
// 获取当前配置
String apiUrl = ApiConfig.getInstance().getApiUrl();
String apiKey = ApiConfig.getInstance().getApiKey();
String model = ApiConfig.getInstance().getApiModel();

// 验证配置有效性
if (ApiConfig.getInstance().isConfigValid()) {
    // 配置有效，可以进行 API 调用
}
```

---

## 💡 常见问题

### Q1: 如何获取 OpenAI API Key？

**A**: 
1. 访问 [platform.openai.com](https://platform.openai.com)
2. 登录或创建账户
3. 转到 API Keys 页面
4. 点击 "Create new secret key"
5. 复制生成的 key（注意：只显示一次）

### Q2: 本地后端应该怎么配置？

**A**:
1. 你需要一个兼容 OpenAI 的本地 API 服务
2. 确保服务运行在 `http://localhost:8080`
3. API 端点应为 `/api/chat`
4. 本地测试时无需 API Key

### Q3: 支持多少个 API 提供商？

**A**: 目前支持 3 个预设提供商：
- OpenAI (官方)
- 本地后端 (开发)
- 自定义 API (第三方)

### Q4: 设置会保存多久？

**A**: 
- 保存在本地 SharedPreferences
- 手机恢复出厂设置时会清除
- 卸载应用时会清除
- 建议定期备份重要配置

### Q5: 配置错误怎么办？

**A**:
1. 检查 URL 格式是否正确
2. 确认 API Key 没有过期或被禁用
3. 使用"🔗 测试连接"验证
4. 查看状态指示器的错误提示
5. 点击"🔄 重置默认"重新开始

---

## 📝 配置示例

### 示例 1: 使用 OpenAI 官方 API

```
选择提供方:   OpenAI (GPT-3.5/GPT-4)
URL:         https://api.openai.com/v1
API 密钥:    sk-proj-1234567890abcdef
模型:        gpt-3.5-turbo

操作: 保存设置 → 测试连接 → 开始聊天
```

### 示例 2: 本地开发测试

```
选择提供方:   本地后端
URL:         http://192.168.1.100:8080
API 密钥:    (留空)
模型:        local

操作: 保存设置 → 确保后端运行 → 开始聊天
```

### 示例 3: 自定义企业 API

```
选择提供方:   自定义 API
URL:         https://company-ai.example.com/v1
API 密钥:    enterprise-key-xxxxx
模型:        company-model-v2

操作: 保存设置 → 测试连接 → 开始聊天
```

---

## 📱 完整流程图

```
应用启动
  ↓
点击"⚙️ API 设置"
  ↓
[设置界面]
  ├─ 选择提供方
  ├─ 输入 API 信息
  ├─ 保存设置
  ├─ 测试连接
  └─ 返回主菜单
  ↓
主菜单 → 点击"💬 开始聊天"
  ↓
发送消息到配置的 API
  ↓
接收 AI 回复并显示
```

---

## 🔄 工作流程

### 首次使用

1. **安装应用** → 首次打开
2. **进来设置** → 配置 API
3. **测试连接** → 验证有效性
4. **开始聊天** → 与 AI 交互

### 修改配置

1. **打开设置** → 主菜单
2. **修改信息** → 更新任何字段
3. **保存设置** → 应用新配置
4. **继续聊天** → 自动使用新 API

### 切换提供商

1. **打开设置** → 主菜单
2. **选择新提供方** → 自动更新提示
3. **填入新凭证** → 输入新 API Info
4. **保存测试** → 验证新配置

---

## 📞 技术支持

### 遇到问题？

1. **检查状态提示** - UI 会显示具体错误
2. **验证网络连接** - 确保设备可以访问外网
3. **测试 API Key** - 确认 Key 在平台上是有效的
4. **查看日志** - 使用 `ApiConfig.setDebugLogging(true)` 激活日志
5. **重置并重试** - 点击"🔄 重置默认"重新配置

### 调试模式

启用调试日志查看详细信息:

```java
ApiConfig.getInstance().setDebugLogging(true);
```

---

## 📌 关键特性总结

✅ **多提供商支持** - OpenAI、本地、自定义
✅ **用户友好界面** - 清晰的表单和实时反馈
✅ **安全密钥管理** - 密码输入框保护隐私
✅ **连接测试** - 验证配置有效性
✅ **本地持久化** - SharedPreferences 保存
✅ **错误提示** - 实时验证和错误消息
✅ **一键重置** - 快速回到默认配置
✅ **灵活扩展** - 支持自定义 API 接入

---

**祝你使用愉快！** 🎉
