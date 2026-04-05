# 🚀 第二阶段网络功能 - 快速开始指南

## 📊 完成状态

✅ **Core 网络功能已实现**

- ✅ ApiConfig - API 端点配置管理
- ✅ RetryInterceptor - 自动重试机制
- ✅ ChatRequest - 增强的请求类（包含宠物上下文）
- ✅ ChatResponse - 增强的响应类（支持多格式）
- ✅ ApiClient - 升级的网络客户端（支持多个回调）
- ✅ ChatActivity - 集成网络 API 调用（自动降级）

---

## 🔧 配置 API

### 方案 A：本地后端（开发测试）

最简单的配置，适合本地开发和测试：

```java
// 在 MainActivity 或 ChatActivity 中配置
ApiClient.configureLocalBackend("http://192.168.1.100:8080");

// 或使用 localhost（仅限模拟器和同机访问）
ApiClient.configureLocalBackend("http://127.0.0.1:8080");
```

**本地后端服务器接口规范**：

```
POST /api/chat

请求体 (JSON):
{
  "message": "用户消息内容",
  "petId": 12345,
  "petInfo": {
    "id": 12345,
    "name": "小白",
    "species": "猫",
    "personality": "温柔、慵懒",
    "speakingStyle": "卖萌、撒娇",
    "appearance": "白色长毛"
  },
  "conversationHistory": [],
  "options": {}
}

响应体 (JSON):
{
  "reply": "你好!我是小白,今天天气真好呢~"
}
```

**Python Flask 示例**：

```python
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/api/chat', methods=['POST'])
def chat():
    data = request.json
    user_message = data.get('message')
    pet_name = data.get('petInfo', {}).get('name', '宠物')
    
    # 简单回复逻辑
    reply = f"{pet_name}说: 你好呀!{user_message}很有趣呢~"
    
    return jsonify({"reply": reply})

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8080)
```

---

### 方案 B：OpenAI API

集成官方 ChatGPT，获得真正的 AI 能力：

**第 1 步：获取 API Key**

1. 访问 https://platform.openai.com/account/api-keys
2. 登录或注册 OpenAI 账户
3. 创建新的 API Key
4. 复制 Key 值

**第 2 步：配置到应用**

```java
String apiKey = "sk-..."; // 从 OpenAI 获取
String model = "gpt-3.5-turbo"; // 或 "gpt-4"

// 在应用启动时配置
ApiClient.configureOpenAI(apiKey, model);
```

**第 3 步：发送聊天消息**

```java
// ChatActivity 中会自动调用
// 不需要额外配置，应用会自动使用 OpenAI API

// 如果需要自定义回调，可以这样：
ApiClient.sendChatMessage(
    "你好吗?",
    petId,
    petInfo,
    new ApiClient.ChatCallback() {
        @Override
        public void onSuccess(String reply) {
            Toast.makeText(context, "AI: " + reply, Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onFailure(String errorMessage) {
            Toast.makeText(context, "错误: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
);
```

**OpenAI 请求格式**（内部自动转换）：

ApiClient 会自动将 ChatRequest 转换为 OpenAI 格式：

```json
POST https://api.openai.com/v1/chat/completions

{
  "model": "gpt-3.5-turbo",
  "messages": [
    {
      "role": "system",
      "content": "你是一个名叫 小白 的 猫 虚拟宠物助手..."
    },
    {
      "role": "user",
      "content": "你好吗?"
    }
  ]
}
```

---

### 方案 C：自定义 API

连接到任何兼容的 API 服务：

```java
String baseUrl = "https://your-api.com/v1";
String apiKey = "your-custom-key";

ApiClient.configureCustom(baseUrl, apiKey);
```

---

## 🧪 测试 API 集成

### 测试 1：验证配置

```java
// 在任何地方调用
String configInfo = ApiClient.getConfigInfo();
Log.d("API", configInfo);

// 输出示例:
// API 配置摘要:
//   提供商: openai
//   URL: https://api.openai.com/v1/chat/completions
//   模型: gpt-3.5-turbo
//   超时: 30 秒
//   重试: 3 次
//   调试: 是
```

### 测试 2：发送聊天消息

```java
// 在聊天页面发送消息时自动测试
// 如果配置无效，akan自动降级到本地模拟

// 查看 Logcat 输出:
// D/ChatActivity: 调用 API: https://api.openai.com/v1/chat/completions
// D/ChatActivity: API 返回成功: 你好呀，我是小白...
```

### 测试 3：测试错误处理

```java
// 尝试不同的错误场景：

// 1. 网络离线
// 预期: 自动降级到本地模拟

// 2. 无效的 API Key
// 预期: 收到 "401 Unauthorized" 错误提示

// 3. API 超时
// 预期: 自动重试 3 次，最终降级到本地

// 4. 格式错误
// 预期: 接收错误消息并显示
```

---

## 🎯 核心功能说明

### 1. 自动降级机制

当 API 不可用时，应用会自动降级到本地模拟：

```
用户发送消息
    ↓
检查 API 配置 → 无效
    ↓
使用本地模拟回复
    ↓
显示结果给用户
```

**控制逻辑** (在 ChatActivity 中):

```java
if (useNetworkApi) {
    requestAIReplyFromApi(text);      // 尝试 API
} else {
    requestAIReplyLocal(text);         // 降级到本地
}

// UI 会自动显示:
// "API 请求失败: ... \n将使用本地回复"
```

### 2. 重试机制

网络错误时自动重试，采用指数退避策略：

```
第 1 次失败 → 等待 1 秒后重试
第 2 次失败 → 等待 2 秒后重试
第 3 次失败 → 等待 4 秒后重试
全部失败 → 返回错误
```

配置可重试错误类型：

```java
// RetryInterceptor 会自动重试：
// • SocketTimeoutException - 连接超时
// • ConnectException - 连接失败
// • SocketException - Socket 错误
// • 网络不稳定相关异常

// 不会重试：
// • 4xx 客户端错误（如 401 无效 Key）
// • SSL 证书错误
// • 协议错误
```

### 3. 加载状态反馈

发送消息时 UI 变化：

```
初始状态:  | 发送 |
            ↓
发送后:    | 加载中... |  （按钮禁用）
            ↓
成功/失败: | 发送 |        （按钮恢复）
```

### 4. 宠物上下文

API 会收到完整的宠物信息，用于生成个性化回复：

```java
ChatRequest.PetInfo petInfo = new ChatRequest.PetInfo(activePet);
// 自动包含:
// • name: 宠物名称
// • species: 物种
// • personality: 性格
// • speakingStyle: 说话风格
// • appearance: 外观
```

---

## 📱 完整使用示例

### 在 MainActivity 中初始化

```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 配置 API（选择一个方案）
        initializeApi();
    }
    
    private void initializeApi() {
        // 开发模式：使用本地后端
        if (BuildConfig.DEBUG) {
            ApiClient.configureLocalBackend("http://10.0.2.2:8080");  // Android 模拟器访问本机
        }
        // 生产模式：使用 OpenAI
        else {
            String apiKey = "sk-...";  // 从配置或 BuildConfig 获取
            ApiClient.configureOpenAI(apiKey, "gpt-3.5-turbo");
        }
        
        // 打印配置信息
        Log.d("API Config", ApiClient.getConfigInfo());
    }
}
```

### 在 ChatActivity 中使用

```java
public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ChatActivity 会自动：
        // 1. 检查 API 配置
        // 2. 根据配置选择 API 或本地
        // 3. 显示相应的提示信息
        // 4. 处理所有错误情况
    }
}
// 无需额外配置！就能使用 API 或自动降级
```

---

## ⚠️ 常见问题

### Q: 我的 API Key 会暴露吗?
**A:** 不会。API Key 确实在app网络请求中使用，但：
1. 所有通信都使用 HTTPS 加密
2. 建议的方式是通过自己的后端代理，而不是直接在 app 中使用
3. 如果直接使用，请定期更换 API Key

### Q: 网络超时怎么办?
**A:** 应用会自动：
1. 重试 3 次（可配置）
2. 每次等待逐倍增长的延迟
3. 最终降级到本地模拟

### Q: 如何切换 API 提供商?
**A:** 

```java
// 动态切换
ApiClient.configureOpenAI("new-key", "gpt-4");
// 自动重新初始化所有连接
```

### Q: 离线模式怎样?
**A:** 应用智能检测：
```java
if (!apiConfig.isConfigValid()) {
    // 自动切换到本地模拟
    useNetworkApi = false;
}
```

### Q: 如何查看详细的网络日志?
**A:** 

```java
// 所有请求/响应都记录在 Logcat 中
// Filter: "ApiClient" 或 "OkHttp"

// 在 Logcat 中查看:
// D/ApiClient: 发送消息: ChatRequest{message='你好'...}
// D/OkHttp3: --> POST https://api.openai.com/v1/chat/completions
// D/OkHttp3: <-- 200 OK (1234ms)
```

---

## 🔄 后续扩展

### Phase 2B：UX 优化
- [ ] 消息加载动画
- [ ] 网络状态指示器
- [ ] 重试按钮

### Phase 2C：数据持久化
- [ ] Room 数据库
- [ ] 聊天记录保存
- [ ] 云端同步

### Phase 2D：监控
- [ ] 错误日志导出
- [ ] 性能统计
- [ ] 远程错误报告

---

## 📞 调试和支持

遇到问题? 检查以下内容：

1. **检查网络**
   ```bash
   # 模拟器访问本机服务
   adb shell netstat -an  # 查看连接
   ```

2. **查看日志**
   ```
   Logcat → Filter: "ChatActivity|ApiClient|OkHttp"
   ```

3. **验证配置**
   ```java
   Log.d("Config", ApiClient.getConfigInfo());
   ```

4. **测试 API**
   ```bash
   # 使用 curl 或 Postman 测试
   curl -X POST http://your-api.com/chat \
     -H "Content-Type: application/json" \
     -d '{"message":"hello","petId":123}'
   ```

---

**现在你已准备好使用第二阶段的网络功能！🚀**

发送第一条消息，体验 AI 回复吧！
