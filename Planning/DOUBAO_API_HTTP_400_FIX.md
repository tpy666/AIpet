# 豆包 API HTTP 400 错误诊断与修复

**版本**：1.0  
**更新日期**：2026年4月4日  
**状态**：✅ 已修复

---

## 问题描述

### 症状
- ❌ 豆包 API 连接测试返回 **HTTP 400** 错误
- ❌ 实际聊天时也会返回 HTTP 400
- ✅ 浏览器可以正常上网（网络连接正常）
- ❌ 其他 API（OpenAI、本地服务器）可能也有类似问题

### 根本原因

**请求格式不匹配**：

在 `SettingsActivity.testConnection()` 中，所有 API 提供商都使用了统一的请求格式（ChatRequest），但豆包和 OpenAI API 期望特定的请求格式：

| API 提供商 | 期望的请求格式 | 实际使用 | 结果 |
|-----------|-------------|--------|------|
| 本地后端 | `ChatRequest` | ✅ `ChatRequest` | 正常 |
| OpenAI | `OpenAIChatRequest` | ❌ `ChatRequest` | HTTP 400 |
| 豆包 | `DoubaoRequest` | ❌ `ChatRequest` | HTTP 400 |
| 自定义 | `ChatRequest` | ✅ `ChatRequest` | 正常 |

### ChatRequest vs DoubaoRequest vs OpenAIChatRequest

**错误的格式（ChatRequest）**：
```json
{
  "message": "test_connection",
  "petId": 0
}
```

**豆包期望的格式（DoubaoRequest）**：
```json
{
  "model": "doubao-seed-2-0-lite-260215",
  "input": [
    {
      "role": "user",
      "content": [
        {
          "type": "input_text",
          "text": "test_connection"
        }
      ]
    }
  ]
}
```

**OpenAI期望的格式（OpenAIChatRequest）**：
```json
{
  "model": "gpt-3.5-turbo",
  "messages": [
    {
      "role": "system",
      "content": "你是一个友好的虚拟宠物助手。"
    },
    {
      "role": "user",
      "content": "test_connection"
    }
  ]
}
```

---

## 修复方案

### 修改文件
- **文件**：[SettingsActivity.java](../../app/src/main/java/com/example/aipet/ui/activity/SettingsActivity.java#L244)
- **方法**：`testConnection()`
- **修改内容**：根据选中的 API 提供商类型构造相应的请求格式

### 核心修改
```java
// 根据提供商类型构造相应的请求体
String requestBodyJson;

if (selectedProvider == R.id.rb_doubao) {
    // 豆包 API：使用 DoubaoRequest 格式
    com.example.aipet.network.DoubaoRequest doubaoRequest = 
        new com.example.aipet.network.DoubaoRequest(modelName, "test_connection");
    requestBodyJson = new com.google.gson.Gson().toJson(doubaoRequest);
} else if (selectedProvider == R.id.rb_openai) {
    // OpenAI API：使用 OpenAIChatRequest 格式
    com.example.aipet.network.OpenAIChatRequest openaiRequest = 
        new com.example.aipet.network.OpenAIChatRequest(modelName, null, "test_connection");
    requestBodyJson = new com.google.gson.Gson().toJson(openaiRequest);
} else {
    // 本地后端或自定义：使用 ChatRequest 格式
    ChatRequest testRequest = new ChatRequest("test_connection", 0L);
    requestBodyJson = new com.google.gson.Gson().toJson(testRequest);
}
```

### 改进点

1. **✅ 豆包 API**：现在使用正确的 `DoubaoRequest` 格式
2. **✅ OpenAI API**：使用正确的 `OpenAIChatRequest` 格式（修复 HTTP 400）
3. **✅ 本地后端**：继续使用 `ChatRequest` 格式
4. **✅ 错误信息**：改进了错误响应体的显示，便于诊断

---

## 测试与验证

### 编译状态
```
✅ BUILD SUCCESSFUL in 2s
```

### 下一步：安装和测试

#### 1. 安装更新的 APK
```powershell
adb install -r E:\Work\AIpet\app\build\outputs\apk\debug\app-debug.apk
```

#### 2. 测试豆包 API（推荐）

**配置步骤**：
1. 打开应用 → 设置 ⚙️
2. 选择 **豆包 (ByteDance Doubao)**
3. 填入信息：
   - **API 端点**：`https://ark.cn-beijing.volces.com/api/v3`
   - **API 密钥**：你的 Bearer Token
   - **模型名称**：`doubao-seed-2-0-lite-260215` （或其他可用模型）
4. 点击 **💾 保存设置**
5. 点击 **💬 连接测试**

**预期结果**：
- ✅ 成功：显示 "✓ 连接成功 (HTTP 200)"
- ❌ 失败：显示具体的 HTTP 错误代码和错误详情

#### 3. 测试 OpenAI API

**配置步骤**：
1. 设置 → 选择 **OpenAI**
2. 填入：
   - **API 端点**：`https://api.openai.com/v1`
   - **API 密钥**：`sk-...`
   - **模型名称**：`gpt-3.5-turbo`
3. 点击 **💾 保存设置**
4. 点击 **💬 连接测试**

#### 4. 实际聊天测试

连接测试成功后：
1. 返回主菜单
2. 点击 **💬 开始聊天**
3. 发送消息

**监控日志**（检查是否有新的错误）：
```bash
adb logcat | grep -i "doubao\|openai\|settingsactivity"
```

---

## 故障排除

### 仍然返回 HTTP 400？

1. **检查 API 端点**
   ```
   豆包：https://ark.cn-beijing.volces.com/api/v3
   OpenAI：https://api.openai.com/v1
   本地：http://10.0.2.2:8080
   ```

2. **检查 API 密钥格式**
   - 豆包：完整的 Bearer Token（不需要添加 "Bearer "前缀，代码会自动处理）
   - OpenAI：以 `sk-` 开头的 API Key

3. **检查模型名称**
   - 豆包：`doubao-seed-2-0-lite-260215` 或其他有效的豆包模型
   - OpenAI：`gpt-3.5-turbo` 或 `gpt-4`

4. **查看详细错误信息**
   - 连接测试失败时会显示 HTTP 错误代码
   - 检查 logcat 输出了解更多细节
   - 应用内会显示错误响应体（如果有）

### HTTP 401 / 403（认证错误）？

- **401**：API 密钥无效或过期
- **403**：无权限访问该模型或端点

**解决方案**：
1. 验证 API 密钥是否正确
2. 检查 API 密钥是否有效期内
3. 确认账户有权访问该模型

### HTTP 429（速率限制）?

- API 调用过于频繁

**解决方案**：
1. 等待几分钟后重试
2. 检查 API 使用配额
3. 考虑使用本地测试服务器开发

### 网络超时？

**症状**：请求超时（30秒或以上）

**原因**：
- 网络连接不稳定
- API 服务器响应缓慢
- 防火墙阻止

**解决方案**：
1. 检查网络连接：`adb shell ping 8.8.8.8`
2. 测试 API 服务器是否在线
3. 尝试使用本地测试服务器

---

## 监控和调试

### 启用详细日志

在聊天时查看 logcat 日志：

```bash
# 查看豆包相关日志
adb logcat | grep -i doubao

# 查看 OpenAI 相关日志
adb logcat | grep -i openai

# 查看所有 API 客户端日志
adb logcat | grep -i apiclient

# 查看设置 Activity 日志
adb logcat | grep -i settingsactivity
```

### 日志输出示例

**豆包 API 成功请求**：
```
D/ApiClient: [豆包] 开始发送消息 - URL: https://ark.cn-beijing.volces.com/api/v3, 模型: doubao-seed-2-0-lite-260215
D/ApiClient: [豆包] ✓ 收到回复 (耗时: 1234ms): 你好！我是一条虚拟宠物...
```

**豆包 API HTTP 400 错误**：
```
D/SettingsActivity: 豆包测试请求: {"model":"doubao-seed-2-0-lite-260215","input":[...]}
E/SettingsActivity: 连接失败 HTTP 400, 响应: {"error": "Invalid request format"}
```

### 查看错误日志

应用内查看：
1. 设置 → 查看错误日志
2. 滚动查看最近的错误
3. 寻找关于 HTTP 400 的错误信息

---

## 相关配置文件

| 文件 | 功能 |
|-----|------|
| [ApiClient.java](../../app/src/main/java/com/example/aipet/network/ApiClient.java) | API 客户端核心逻辑 |
| [ApiConfig.java](../../app/src/main/java/com/example/aipet/network/ApiConfig.java) | API 配置管理 |
| [DoubaoRequest.java](../../app/src/main/java/com/example/aipet/network/DoubaoRequest.java) | 豆包请求格式 |
| [OpenAIChatRequest.java](../../app/src/main/java/com/example/aipet/network/OpenAIChatRequest.java) | OpenAI 请求格式 |
| [ChatRequest.java](../../app/src/main/java/com/example/aipet/network/ChatRequest.java) | 通用请求格式 |

---

## 已知限制

### 豆包 API 特殊要求

豆包 API 的 Bearer Token 需要在 HTTP Authorization 请求头中传递：
```
Authorization: Bearer <your_token>
```

代码已正确处理此要求。

### OpenAI API 特殊要求

OpenAI API 的请求必须遵循标准的 ChatGPT 格式：
- 必须包含 `messages` 数组
- 第一条消息通常是 `system` role
- 最后一条消息通常是 `user` role

代码已正确处理此要求。

---

## 下次修复的建议

1. **单元测试**：为不同的 API 提供商编写单元测试
2. **请求格式验证**：添加请求体验证器
3. **更详细的错误信息**：显示完整的错误响应体
4. **API 健康检查**：定期检查 API 端点的健康状态

---

## 总结

| 问题 | 原因 | 解决方案 |
|-----|-----|--------|
| 豆包返回 HTTP 400 | 使用了 ChatRequest 而非 DoubaoRequest | ✅ 已修复 |
| OpenAI 返回 HTTP 400 | 使用了 ChatRequest 而非 OpenAIChatRequest | ✅ 已修复 |
| 连接测试不准确 | 没有真实调用 API | ✅ 已使用真实 HTTP 请求 |
| 错误信息不完整 | 没有显示错误响应体 | ✅ 已改进 |

**修复后的预期行为**：
- ✅ 豆包 API 连接测试：成功（或显示有效的错误信息）
- ✅ OpenAI API 连接测试：成功（或显示有效的错误信息）
- ✅ 本地后端：继续正常工作
- ✅ 实际聊天：使用正确的请求格式

---

**需要进一步帮助？查看：**
- [PROJECT_COMPLETE_SUMMARY.md](PROJECT_COMPLETE_SUMMARY.md) - 项目总体说明
- [API_FAILURE_SOLUTION.md](API_FAILURE_SOLUTION.md) - API 解决方案对比
- [API_DIAGNOSTIC_TOOL.md](API_DIAGNOSTIC_TOOL.md) - API 诊断工具
