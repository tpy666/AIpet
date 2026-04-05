# 🎯 API 请求失败 - 解决方案总结

**问题：** 您的应用 API 请求失败（可能是因为模拟器没有外部网络）  
**解决方案：** 3 个立即可用的方案

---

## 🚀 立即行动（选择一个）

### ✅ 方案 1：使用本地测试服务器（推荐 - 5 分钟搞定）

**为什么选这个？** 最快、最简单、无需网络

**操作步骤：**

```bash
# 1️⃣ 打开 Windows PowerShell，进入项目目录
cd E:\Work\AIpet

# 2️⃣ 启动本地 API 服务器
cd test-backend
.\run_server.bat

# 3️⃣ 在 Android Studio 中运行应用
# → 菜单栏 Run → Run 'app' (或 SHIFT+F10)

# 4️⃣ 发送消息测试
# 在应用中输入任何消息 → 立即收到宠物回复 ✅
```

**效果：**
```
你说: "你好"
宠物说: "喵~我好像不太感兴趣呢我是美美，很高兴认识你！"
```

📖 **详细指南：** 查看 [`QUICK_START_LOCAL_TEST.md`](QUICK_START_LOCAL_TEST.md)  
🔍 **问题排查：** 查看 [`API_DIAGNOSTIC_TOOL.md`](API_DIAGNOSTIC_TOOL.md)

---

### ✅ 方案 2：配置真实 API（需要网络 - 10 分钟）

**为什么选这个？** 如果你有网络连接和 AI API Key

**操作步骤：**

```
1. 获取 API Key
   → 访问 https://platform.openai.com/api-keys
   → 创建新 Key（例如：sk-xxx...xxx）

2. 在应用中配置
   → 打开应用 → 点击右上角 ⚙️ 按钮
   → 选择"OpenAI"提供方
   → 输入 API Key
   → 点击"保存设置"

3. 测试
   → 发送任何消息
   → 收到真实 AI 回复 ✅
```

📖 **详细指南：** 查看 [`API_DIAGNOSTIC_TOOL.md` 中的"方案 B"](API_DIAGNOSTIC_TOOL.md)

---

### ✅ 方案 3：修复模拟器网络（高级 - 15 分钟）

**为什么选这个？** 想让模拟器能访问外部网络

**操作步骤：**

```bash
# 1. 检查网络状态
adb shell settings get global airplane_mode_on
# 应该输出: 0

# 2. 启用数据流量
adb shell settings put global mobile_data 1
adb shell settings put global data_roaming 1

# 3. 重启模拟器
adb reboot

# 4. 验证网络
adb shell ping -c 4 8.8.8.8
```

📖 **详细指南：** 查看 [`API_DIAGNOSTIC_TOOL.md` 中的"方案 C"](API_DIAGNOSTIC_TOOL.md)

---

## 📊 方案对比

| 特性 | 方案 1 (本地) | 方案 2 (真实API) | 方案 3 (修复网络) |
|------|--------------|-----------------|-----------------|
| 难度 | ⭐ 最简单 | ⭐⭐ 中等 | ⭐⭐⭐ 复杂 |
| 时间 | 5 分钟 | 10 分钟 | 15 分钟 |
| 需要网络 | ❌ 否 | ✅ 是 | ✅ 是 |
| 需要 API Key | ❌ 否 | ✅ 是 | ❌ 否 |
| 测试效果 | ✅ 很好 | ✅✅ 最好 | ✅ 很好 |

**建议：** 新手选**方案 1**，熟悉后选**方案 2**

---

## 📁 相关文件位置

```
项目根目录/
├── API_DIAGNOSTIC_TOOL.md           ← 📖 完整诊断指南
├── QUICK_START_LOCAL_TEST.md        ← 📖 本地测试快速开始
├── DIAGNOSTIC_TOOL_USAGE.md         ← 📖 诊断工具使用说明
└── test-backend/
    ├── local_api_server.py          ← 🐍 Python 本地服务器
    ├── requirements.txt             ← 📦 Python 依赖
    ├── run_server.bat               ← 🖥️ Windows 启动脚本
    └── run_server.sh                ← 🐧 Mac/Linux 启动脚本
```

---

## 🔍 诊断检查表

如果按照上述步骤仍未解决，按此检查：

```
☐ 1. 本地服务是否启动？
     → 命令: cd test-backend && .\run_server.bat
     → 检查: 看到"Running on http://..."表示成功

☐ 2. 应用是否配置了正确的 URL？
     → 本地: http://10.0.2.2:8000
     → OpenAI: https://api.openai.com/v1

☐ 3. 模拟器是否能访问？
     → 命令: adb shell ping 10.0.2.2
     → 期望: 收到响应

☐ 4. 应用是否有网络权限？
     → 检查: AndroidManifest.xml 包含 INTERNET 权限

☐ 5. 防火墙是否阻止连接？
     → 检查: Windows 防火墙规则
```

---

## 🆘 常见问题速查

### Q: "Connection refused"?
**A:** 本地服务未启动，运行 `run_server.bat`

### Q: "Connection timeout"?
**A:** 检查 URL 是否正确，或网络是否连接

### Q: "Empty response"?
**A:** 检查请求体格式，或查看服务器日志

### Q: "401/403 Unauthorized"?
**A:** API Key 无效或过期

### Q: 应用仍无法收到回复?
**A:** 查看 Logcat：`adb logcat | grep ApiClient`

---

## ✅ 完成验证

当你成功配置后，应该看到：

```
✅ 应用启动
✅ 输入消息
✅ 点击发送
✅ 立即收到宠物回复（无加载延迟）
✅ 回复显示在聊天列表
✅ 按钮恢复可用
```

---

## 📞 获取详细帮助

根据你选择的方案，查看对应文档：

- **本地测试（推荐）**→ [`QUICK_START_LOCAL_TEST.md`](QUICK_START_LOCAL_TEST.md)
- **诊断工具**→ [`API_DIAGNOSTIC_TOOL.md`](API_DIAGNOSTIC_TOOL.md)
- **实施细节**→ [`DIAGNOSTIC_TOOL_USAGE.md`](DIAGNOSTIC_TOOL_USAGE.md)

---

## 🎯 下一步

1. **现在就试：** 选择方案 1 并启动本地服务器（5 分钟）
2. **开始测试：** 在应用中发送消息并验证回复
3. **扩展功能：** 一旦工作正常，升级到真实 API
4. **部署上线：** 配置生产环境 API

---

**祝你成功！** 🎉

任何问题，请参考上面的文档或查看应用日志。

