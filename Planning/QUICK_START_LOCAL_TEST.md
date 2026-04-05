# 🚀 AIpet API 测试 - 3 分钟快速开始

## 🎯 目标

在 3 分钟内让应用成功接收 AI 回复，无需外部网络。

---

## ⚡ 快速开始（3 步）

### Step 1️⃣：启动本地 API 服务器（30 秒）

**Windows：**
```bash
cd E:\Work\AIpet\test-backend
run_server.bat
```

**Mac/Linux：**
```bash
cd ~/AIpet/test-backend
chmod +x run_server.sh
./run_server.sh
```

**预期输出：**
```
╔══════════════════════════════════════════════════════════════╗
║          AIpet 本地 API 测试服务器已启动                    ║
╚══════════════════════════════════════════════════════════════╝

📱 配置应用: ApiClient.configureLocalBackend("http://10.0.2.2:8000");
🚀 运行在 http://0.0.0.0:8000
```

✅ **确认：** 看到这个输出说明服务已启动成功！

---

### Step 2️⃣：配置应用使用本地服务（1 分钟）

**方式 A：在代码中配置（最简单）**

编辑 `src/main/java/com/example/aipet/ui/activity/ChatActivity.java`：

找到 `onCreate` 方法，添加以下代码：

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    
    // ✅ 添加这一行
    ApiClient.configureLocalBackend("http://10.0.2.2:8000");
    
    // ... 其他初始化代码
}
```

**方式 B：通过 SettingsActivity 配置（推荐）**

1. 打开应用
2. 点击右上角 ⚙️ **API 设置**
3. 选择 **"本地后端"**
4. 输入 URL：`http://10.0.2.2:8000`
5. 点击 💾 **保存设置**

**方式 C：自动配置（开发模式）**

编辑 `src/main/java/com/example/aipet/network/ApiConfig.java`：

```java
private ApiConfig() {
    // 初始化默认配置
    initializeDefaults();
    
    // ✅ 开发时自动使用本地服务
    this.currentProvider = ApiProvider.LOCAL_BACKEND;
    this.customBaseUrl = "http://10.0.2.2:8000";
}
```

✅ **确认：** 选择一种方式即可

---

### Step 3️⃣：运行并测试应用（1 分钟 30 秒）

**Build 应用：**
```bash
cd E:\Work\AIpet
./gradlew build
```

**运行应用：**
```
Android Studio → Run → Run 'app'
或 SHIFT + F10
```

**发送测试消息：**

1. 应用启动后，进入 **ChatActivity**
2. 创建或选择一个宠物
3. 输入任何说辞：`你好` / `hi` / `测试`
4. 点击 **发送**
5. 应该立即收到宠物的回复 ✅

**预期回复示例：**
```
用户: 你好
宠物: 喵~我好像不太感兴趣呢我是美美，很高兴认识你！
```

---

## ✅ 检查清单

```
✅ 本地服务运行在 http://localhost:8000
✅ 应用配置使用 http://10.0.2.2:8000
✅ 应用已构建并安装
✅ 模拟器已启动
✅ 宠物数据已存在
✅ 发送消息后收到回复
```

---

## 🔍 排查问题

### 问题 1："Connection refused"

**检查：** 本地服务是否启动？

```bash
# 查看是否有进程监听 8000 端口
netstat -an | grep 8000
# 或
lsof -i :8000
```

**解决：** 重新启动服务器

```bash
cd E:\Work\AIpet\test-backend
run_server.bat
```

---

### 问题 2："模拟器无法连接 10.0.2.2"

**原因：** 这是 Android 模拟器的特殊 IP，指向主机 localhost

**解决选项：**

#### 选项 A：使用正确的 IP（推荐）

```java
// 对于 Windows/Mac/Linux，10.0.2.2 总是指向主机
ApiClient.configureLocalBackend("http://10.0.2.2:8000");
```

#### 选项 B：使用主机 IP 地址

```bash
# 查看你的电脑 IP
ipconfig          # Windows
ifconfig          # Mac/Linux

# 例如：192.168.1.100
ApiClient.configureLocalBackend("http://192.168.1.100:8000");
```

---

### 问题 3："发送消息后没有回复"

**检查日志：**

```bash
adb logcat | grep -i "ApiClient\|ChatActivity"
```

**预期日志输出：**
```
I/ApiClient: 调用 API: http://10.0.2.2:8000/api/chat
D/ApiClient: 请求体: {...}
I/ApiClient: API 返回成功: {choices: [{message: {content: "喵~..."}}]}
```

**如果看到错误：** 查看服务器端的输出（在服务器运行窗口）

```
[2024-04-04 10:30:45] POST /api/chat
Request body: {message: "你好", petId: 1, ...}
Response (200): {id: "chatcmpl-...", choices: [...]}
```

---

### 问题 4："500 Internal Server Error"

**检查：** Python 依赖是否正确安装

```bash
cd E:\Work\AIpet\test-backend
pip install -r requirements.txt
```

**重启服务：**
```bash
run_server.bat
```

---

## 📊 工作流程验证

当你成功配置后，应该看到这样的流程：

```
┌─────────────────────────────────────────────────────────┐
│ 1. 用户在应用中输入消息                                 │
│    ↓                                                      │
│ 2. ChatActivity 收集消息和宠物信息                      │
│    ↓                                                      │
│ 3. ApiClient 通过 Retrofit 发送 HTTP POST 请求         │
│    ↓                                                      │
│ 4. 请求到达本地服务 (http://10.0.2.2:8000/api/chat)  │
│    ↓                                                      │
│ 5. Python Flask 处理请求                               │
│    ↓                                                      │
│ 6. 返回宠物个性化回复 JSON                              │
│    ↓                                                      │
│ 7. Retrofit 解析响应                                    │
│    ↓                                                      │
│ 8. ChatActivity 显示回复                                │
│    ↓                                                      │
│ ✅ 用户看到宠物的回复                                   │
└─────────────────────────────────────────────────────────┘
```

---

## 🎓 下一步

完成本地测试后，可以：

1. **集成真实 API**
   - 获取 OpenAI API Key
   - 配置应用使用 OpenAI
   - 测试真实 AI 回复

2. **优化应用**
   - 改进 UI 动画
   - 添加加载指示器
   - 实现聊天记录保存

3. **部署上线**
   - 配置生产环境 API
   - 优化性能
   - 提交 Google Play

---

## 📞 获取帮助

如果还有问题，查看：

- **诊断工具：** `API_DIAGNOSTIC_TOOL.md`
- **API 文档：** 服务器返回 `GET http://localhost:8000/`
- **日志文件：** `adb logcat | grep ApiClient`

---

**祝测试顺利！** 🎉

