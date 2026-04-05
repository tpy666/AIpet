# 🔧 ChatActivity 网络连接诊断工具

## 功能说明

这个诊断工具可以帮助你快速定位 API 请求失败的原因。

---

## 使用方法

### 1. 添加诊断方法到 ChatActivity

在 `src/main/java/com/example/aipet/ui/activity/ChatActivity.java` 中添加以下代码：

```java
/**
 * 诊断 API 连接状态
 */
private void diagnoseApiConnection() {
    new Thread(() -> {
        try {
            logDebug("开始 API 诊断...");
            
            // 1. 检查 API 配置
            ApiConfig config = ApiClient.getApiConfig();
            logDebug("API 提供商: " + config.getCurrentProvider());
            logDebug("API URL: " + config.getApiUrl());
            logDebug("配置有效: " + config.isConfigValid());
            
            if (!config.isConfigValid()) {
                logWarning("❌ API 配置无效: " + config.getValidationError());
                return;
            }
            
            // 2. 测试网络连接
            logDebug("测试网络连接...");
            String apiUrl = config.getApiUrl();
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            logDebug("HTTP 响应码: " + responseCode);
            
            if (responseCode == 200) {
                logDebug("✅ 网络连接正常");
            } else {
                logWarning("⚠️  网络连接异常: " + responseCode);
            }
            
            conn.disconnect();
            
            // 3. 测试 API 调用
            logDebug("测试 API 调用...");
            ChatRequest testRequest = new ChatRequest(
                "测试",
                activePet != null ? activePet.getId() : 1,
                activePet != null ? new ChatRequest.PetInfo(activePet) : null
            );
            
            Call<ChatResponse> call = ApiClient.getApiService().sendMessage(testRequest);
            Response<ChatResponse> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                logDebug("✅ API 测试成功");
                logDebug("响应内容: " + response.body().toString());
            } else {
                logWarning("❌ API 测试失败: " + response.code() + " " + response.message());
            }
            
        } catch (Exception e) {
            logWarning("❌ 诊断异常: " + e.getMessage());
            e.printStackTrace();
        }
    }).start();
}

/**
 * 从菜单调用诊断工具
 */
@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_diagnosis) {
        diagnoseApiConnection();
        return true;
    }
    // ... 其他菜单项
    return super.onOptionsItemSelected(item);
}
```

### 2. 在菜单中添加诊断选项

编辑 `src/main/res/menu/menu_chat.xml`（或在 onCreateOptionsMenu 中动态添加）：

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/menu_diagnosis"
        android:title="🔍 API 诊断"
        android:showAsAction="never" />
</menu>
```

### 3. 运行诊断

1. 打开 ChatActivity
2. 点击右上角菜单 (⋮)
3. 选择 "🔍 API 诊断"
4. 查看 Logcat 输出

---

## 诊断输出示例

### ✅ 成功的诊断

```
D/ChatActivity: 开始 API 诊断...
D/ChatActivity: API 提供商: openai
D/ChatActivity: API URL: https://api.openai.com/v1/chat/completions
D/ChatActivity: 配置有效: true
D/ChatActivity: 测试网络连接...
D/ChatActivity: HTTP 响应码: 200
D/ChatActivity: ✅ 网络连接正常
D/ChatActivity: 测试 API 调用...
D/ChatActivity: ✅ API 测试成功
D/ChatActivity: 响应内容: ChatResponse{...}
```

### ❌ 失败的诊断

```
D/ChatActivity: 开始 API 诊断...
D/ChatActivity: API 提供商: LOCAL_BACKEND
D/ChatActivity: API URL: http://127.0.0.1:8080/api/chat
D/ChatActivity: 配置有效: true
D/ChatActivity: 测试网络连接...
W/ChatActivity: ❌ API 诊断异常: Connection refused
```

---

## 常见问题排查

### 问题：Connection refused

**原因：**
- 本地服务未启动
- 服务运行在错误的端口

**解决：**
1. 检查本地服务是否启动
2. 确认端口号正确

### 问题：Connection timeout

**原因：**
- 网络断开
- 防火墙阻止
- API 服务响应缓慢

**解决：**
1. 检查网络连接
2. 检查防火墙设置
3. 增加超时时间

### 问题：HTTP 401/403

**原因：**
- API Key 无效
- 缺少认证信息

**解决：**
1. 检查 API Key
2. 检查认证头

### 问题：HTTP 500

**原因：**
- 服务器错误
- 请求格式错误

**解决：**
1. 查看服务器日志
2. 检查请求体格式

---

## 自动化诊断脚本

对于快速诊断，可以创建一个诊断活动：

```java
public class DiagnosticActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 运行所有诊断
        runFullDiagnostics();
    }
    
    private void runFullDiagnostics() {
        StringBuilder report = new StringBuilder();
        
        // 1. 系统信息
        report.append("=== 系统信息 ===\n");
        report.append("Android API: ").append(Build.VERSION.SDK_INT).append("\n");
        report.append("设备型号: ").append(Build.MODEL).append("\n");
        
        // 2. 网络信息
        report.append("\n=== 网络信息 ===\n");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        report.append("网络连接: ").append(ni != null && ni.isConnected() ? "✅" : "❌").append("\n");
        report.append("网络类型: ").append(ni != null ? ni.getTypeName() : "无").append("\n");
        
        // 3. API 配置
        report.append("\n=== API 配置 ===\n");
        ApiConfig config = ApiClient.getApiConfig();
        report.append("提供商: ").append(config.getCurrentProvider()).append("\n");
        report.append("URL: ").append(config.getApiUrl()).append("\n");
        report.append("有效: ").append(config.isConfigValid() ? "✅" : "❌").append("\n");
        
        // 显示诊断报告
        showDiagnosticReport(report.toString());
    }
}
```

---

## 📋 诊断检查清单

在运行诊断前，请确认以下项目：

```
☐ 模拟器已启动
☑ Android Studio 已连接模拟器
☐ 应用已安装
☐ Logcat 已打开
☐ 如果使用本地服务，服务已启动
☐ 如果使用真实 API，API Key 已配置
```

---

## 📊 诊断结果解读

| 诊断项 | 成功标志 | 失败标志 | 解决方案 |
|--------|---------|---------|---------|
| API 配置 | ✅ Valid | ❌ Invalid | 检查 SettingsActivity 配置 |
| 网络连接 | ✅ HTTP 200 | ❌ Connection refused | 检查网络和防火墙 |
| 服务连接 | ✅ 服务响应 | ❌ Timeout | 启动本地服务 |
| API 调用 | ✅ 有响应 | ❌ 无响应 | 检查请求格式 |

---

**祝诊断顺利！** 🔍

