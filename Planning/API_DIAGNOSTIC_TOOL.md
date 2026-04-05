# 🔍 API 诊断工具 - 快速排查指南

## 问题诊断

### 问题 1：API 请求失败

**检查清单：**

```
☐ 1. 模拟器是否有网络连接？
☐ 2. API 配置是否正确？
☐ 3. API Key 是否有效？
☐ 4. 防火墙是否阻止了连接？
☐ 5. API 端点是否正确？
```

### 问题 2：模拟器无法访问外部网络

**症状：**
- 应用显示 "API 请求失败"
- 自动降级到本地模拟模式
- 模拟器 Ping 失败

**解决方案 → 见下文"方案选择"**

---

## 📱 方案选择

根据你的情况，选择合适的方案：

### 方案 A：快速本地测试（推荐 ⭐⭐⭐）

**适用场景：**
- 进行本地开发测试
- 没有真实 API Key
- 快速验证应用逻辑

**操作步骤：**

#### Step 1: 启动本地测试服务

```bash
# Windows PowerShell
cd E:\Work\AIpet\test-backend
python local_api_server.py
```

输出示例：
```
 * Running on http://127.0.0.1:8000
 * Press CTRL+C to quit
```

#### Step 2: 配置应用使用本地服务

在应用中（SettingsActivity 或代码中）：
```java
// 自动检测：应用首次启动时会尝试连接本地服务
// 或手动配置：
ApiClient.configureLocalBackend("http://10.0.2.2:8000");
```

#### Step 3: 发送测试消息

在应用中发送任何消息，应该立即收到本地模拟回复 ✅

---

### 方案 B：真实网络测试（需要网络）

**适用场景：**
- 拥有有效的 API Key
- 电脑和模拟器都有网络
- 要测试真实 API

**操作步骤：**

#### Step 1: 获取 API Key

从以下服务获取：
- OpenAI: https://platform.openai.com/api-keys
- 其他服务: 按照对应文档

#### Step 2: 配置应用

在 SettingsActivity 中：
```
1. 点击 ⚙️ API 设置
2. 选择 "OpenAI" 提供方
3. 输入 API Key
4. 点击 💾 保存设置
```

或代码配置：
```java
ApiClient.configureOpenAI("sk-...", "gpt-3.5-turbo");
```

#### Step 3: 检查网络连接

```bash
# Windows PowerShell - 检查模拟器网络
adb shell ping -c 4 8.8.8.8
```

期望输出：
```
PING 8.8.8.8 (8.8.8.8): 56 data bytes
64 bytes from 8.8.8.8: seq=0 ttl=64 time=42.123 ms
...
```

#### Step 4: 调试日志

在 Android Studio 中查看 Logcat：
```
adb logcat | grep -i "ApiClient\|ChatActivity"
```

应该看到：
```
I/ApiClient: 调用 API: https://api.openai.com/v1/chat/completions
D/ApiClient: 请求体: {...}
I/ApiClient: API 返回成功: {...}
```

---

### 方案 C：模拟器网络修复

**如果模拟器网络本身有问题：**

#### 步骤 1: 确认网络连接

```bash
adb shell settings get global airplane_mode_on
# 期望输出: 0 (表示飞行模式关闭)
```

#### 步骤 2: 启用数据流量

```bash
adb shell settings put global mobile_data 1
adb shell settings put global data_roaming 1
```

#### 步骤 3: 重启模拟器

```bash
adb reboot
```

#### 步骤 4: 验证网络

```bash
adb shell am start -a android.intent.action.VIEW -d http://www.google.com
```

---

## 🔧 Android Studio 诊断工具

### 查看详细日志

1. **打开 Logcat**
   ```
   View → Tool Windows → Logcat
   ```

2. **过滤相关日志**
   ```
   搜索 "ApiClient" 或 "ChatActivity"
   ```

3. **查看网络请求**
   ```
   Android Profiler → Network
   ```

### 使用 adb 命令诊断

```bash
# 查看所有网络连接
adb shell netstat

# 查看 DNS 设置
adb shell getprop net.dns

# 测试 DNS 解析
adb shell nslookup api.openai.com

# 查看应用权限
adb shell pm dump com.example.aipet | grep -i permission

# 查看应用网络状态
adb shell dumpsys connectivity
```

---

## 📊 自动化诊断脚本

### 运行自动诊断（推荐）

```bash
# Windows PowerShell
.\gradlew runDiagnostics
```

或手动运行：

```bash
# 1. 启动模拟器
emulator -avd Pixel_4_API_33

# 2. 等待启动完成
adb wait-for-device

# 3. 运行诊断
adb shell am start -a com.example.aipet.DIAGNOSE

# 4. 查看结果
adb logcat | grep -i "DIAGNOSTIC"
```

---

## ✅ 常见问题排查

### Q1: "Connection refused"
**原因：** 本地服务未启动或端口错误
**解决：** 确保本地服务运行在 127.0.0.1:8000

### Q2: "Connection timeout"
**原因：** 网络无法连接到 API
**解决：** 
- 检查防火墙
- 检查 VPN
- 检查 DNS

### Q3: "SSL Certificate Error"
**原因：** HTTPS 证书验证失败
**解决：** 对于开发，在 HttpClient 中禁用证书验证（仅开发用！）

### Q4: "Empty Response"
**原因：** API 响应格式不正确
**解决：** 检查请求体格式是否符合 API 要求

### Q5: "Authentication Failed"
**原因：** API Key 无效或过期
**解决：** 重新检查 API Key

---

## 🚀 快速修复

### 如果一切都失败了，使用本地模拟模式

```java
// 在 ChatActivity.java 中
private static final boolean FORCE_LOCAL_MODE = false;  // 改为 true

private void checkApiConfiguration() {
    if (FORCE_LOCAL_MODE) {
        useNetworkApi = false;  // 强制使用本地模拟
        return;
    }
    // ... 正常逻辑
}
```

---

## 📞 下一步

1. **立即尝试：** 启动本地测试服务（方案 A）
2. **调试问题：** 使用诊断工具
3. **配置 API：** 部署方案 B
4. **反馈：** 如有问题，查看日志并记录错误信息

---

**需要帮助？** 
- 查看详细日志：`adb logcat | grep ApiClient`
- 检查网络连接：`adb shell ping 8.8.8.8`
- 验证服务状态：访问 `http://localhost:8000` (如使用本地服务)

