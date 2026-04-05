# 🚀 第二阶段：网络端口开发计划

## 📋 项目状态回顾

**当前状态**：
- ✅ UI 界面布局完整（5 个 Activity）
- ✅ 数据模型定义完整（Pet + Message）
- ✅ 本地模拟 AI 回复已实现
- ⏳ **缺失**：真实网络 API 集成

**第一阶段完成度**：95%（仅缺 API 配置）

---

## 🎯 第二阶段目标

将应用从**本地模拟模式**升级为**真实网络通信模式**，实现以下功能：

1. **增强 ApiClient** - 支持更复杂的聊天请求
2. **完善错误处理** - 网络超时、异常、重试机制
3. **优化用户体验** - 加载动画、网络状态提示
4. **支持多个 API 源** - OpenAI、本地后端、其他云服务
5. **持久化聊天记录** - 使用 Room 数据库存储对话

---

## 📝 详细任务列表

### Phase 2A：Core 网络功能（优先级：⭐⭐⭐）

#### 任务 1：升级 ApiClient
**文件**: `ApiClient.java`
**目标**:
```
✓ 支持多个 API 端点配置
✓ 增强 ChatRequest 包含宠物信息上下文
✓ 添加请求头（User-Agent、API Key、Content-Type）
✓ 实现重试机制（最多 3 次）
✓ 添加请求超时设置（30 秒）
✓ 支持 POST 和 GET 方法
```

**关键改变**:
- 定义 ApiConfig 类管理 API 端点和密钥
- 增强 ChatRequest：包含 petInfo 上下文
- 实现 RetryInterceptor 自动重试
- 完善 ChatResponse 响应体结构

---

#### 任务 2：集成 ChatActivity 真实 API 调用
**文件**: `ChatActivity.java`
**目标**:
```
✓ 替换本地 generatePetReply() 为 API 调用
✓ 显示"加载中..."动画
✓ 处理网络异常，显示友好错误提示
✓ 支持网络离线、API 失败时的降级方案
✓ 记录 API 请求/响应日志
```

**关键改变**:
- 移除 `generatePetReply()` 方法或将其作为降级方案
- 调用 `ApiClient.sendChatMessage()`
- UI 状态管理：加载中、成功、失败
- 错误回调处理

---

#### 任务 3：添加网络配置类
**新文件**: `ApiConfig.java`
**功能**:
```java
class ApiConfig {
    // API 端点选择
    enum ApiProvider {
        OPENAI("https://api.openai.com/v1/chat/completions"),
        LOCAL_BACKEND("http://127.0.0.1:8080/api/chat"),
        CUSTOM("https://your-custom-api.com/chat")
    }
    
    // 配置管理
    ✓ getApiUrl()          获取当前 API 端点
    ✓ setApiProvider()     切换 API 提供商
    ✓ getApiKey()          获取 API 授权 Key
    ✓ setApiKey()          设置 API 授权 Key
    ✓ getTimeout()         获取超时时间
    ✓ isNetworkEnabled()   检查网络可用性
}
```

---

### Phase 2B：用户体验优化（优先级：⭐⭐）

#### 任务 4：添加加载状态 UI
**文件**: `activity_chat.xml` + `ChatActivity.java`
**目标**:
```
✓ 发送消息后显示"thinking..."动画
✓ 消息项中显示加载进度
✓ 网络错误显示重试按钮
✓ 超时友好提示
```

**实现方式**:
- 在 RecyclerView 中插入 Loading MessageType
- 使用 ProgressBar 或动画效果
- Swipe to Retry 功能

---

#### 任务 5：离线/降级方案
**文件**: `ChatActivity.java`
**目标**:
```
✓ 如果网络不可用，提示用户
✓ 提供选项：使用本地模拟、离线模式
✓ 缓存最近的 API 响应
✓ 队列化离线消息，网络恢复时重试
```

---

### Phase 2C：数据持久化（优先级：⭐⭐）

#### 任务 6：集成 Room 数据库
**新文件**: 
- `ChatHistory.java` (Entity)
- `ChatHistoryDao.java` (DAO)
- `AppDatabase.java` (Database)

**功能**:
```
✓ 保存每条消息到数据库
✓ 按 petId 查询聊天记录
✓ 支持导出、删除、搜索聊天历史
✓ 自动同步到 Cloud（可选）
```

---

#### 任务 7：消息同步工具
**新文件**: `ChatSyncManager.java`
**功能**:
```
✓ 定期上传本地消息到后端
✓ 下载云端消息同步到本地
✓ 处理冲突（本地/云端消息版本）
✓ 后台同步服务
```

---

### Phase 2D：监控和日志（优先级：⭐）

#### 任务 8：添加网络日志工具
**新文件**: `NetworkLogger.java`
**功能**:
```
✓ 记录所有 API 请求/响应
✓ 记录错误信息、堆栈跟踪
✓ 导出日志文件用于调试
✓ 集成远程错误报告（Crashlytics 可选）
```

---

## 🔄 实现顺序

### Week 1：Core 功能
```
1. ApiConfig.java          (2h)   - API 端点配置
2. 增强 ApiClient.java     (3h)   - 重试、超时、错误处理
3. ChatActivity 集成       (3h)   - 调用真实 API
4. 测试网络调用           (2h)   - 单元/集成测试
```

### Week 2：用户体验
```
5. 加载状态 UI            (2h)   - ProgressBar、动画
6. 错误处理 UI            (2h)   - 重试、离线提示
7. 降级方案               (2h)   - 本地模拟备份
8. 测试 UX               (1h)   - 用户体验测试
```

### Week 3：持久化
```
9. Room 集成              (3h)   - 聊天记录数据库
10. ChatSyncManager       (3h)   - 消息同步
11. 数据库迁移            (1h)   - 版本管理
```

### Week 4：优化和发布
```
12. NetworkLogger         (2h)   - 日志工具
13. 性能测试              (2h)   - 内存、电池、流量
14. 文档更新              (1h)   - README、API 文档
15. 发布 v2.0.0          (1h)   - 版本管理
```

---

## 🛠 技术栈补充

### 新增依赖
```gradle
// Room 数据库（任务 6-7）
implementation("androidx.room:room-runtime:2.6.1")
annotationProcessor("androidx.room:room-compiler:2.6.1")

// WorkManager - 后台同步（可选）
implementation("androidx.work:work-runtime:2.8.1")

// Crashlytics - 远程错误报告（可选）
implementation("com.google.firebase:firebase-crashlytics:18.6.3")

// 日志库
implementation("com.jakewharton.timber:timber:5.0.1")
```

---

## 📊 验收标准

| 任务 | 验收条件 |
|------|---------|
| 任务 1 | ✅ ApiClient 支持重试、超时、多端点 |
| 任务 2 | ✅ ChatActivity 成功调用真实 API |
| 任务 3 | ✅ ApiConfig 可灵活切换 API 源 |
| 任务 4 | ✅ UI 显示加载状态、成功、错误 |
| 任务 5 | ✅ 离线时有友好提示和降级方案 |
| 任务 6 | ✅ Room 数据库正确存储消息 |
| 任务 7 | ✅ 消息能正确同步 |
| 任务 8 | ✅ 日志能记录并导出调试 |

---

## 🚨 风险和注意事项

| 风险 | 缓解方案 |
|------|---------|
| **API 配额限制** | 实现消息队列、速率限制 |
| **网络延迟** | 设置合理超时、重试机制 |
| **数据同步冲突** | 使用时间戳、版本号解决 |
| **隐私/安全** | API Key 存储加密、HTTPS 通信 |
| **用户流量** | 压缩请求体、缓存响应 |

---

## 📚 参考资源

- Retrofit 文档：https://square.github.io/retrofit/
- OkHttp 文档：https://square.github.io/okhttp/
- Room 数据库：https://developer.android.com/training/data-storage/room
- WorkManager：https://developer.android.com/topic/libraries/architecture/workmanager
- Timber 日志：https://github.com/JakeWharton/timber

---

## ✅ 第二阶段完成标志

当以下条件满足时，第二阶段完成：

```
✅ ApiClient 支持真实 API 调用
✅ ChatActivity 成功从 API 获取 AI 回复
✅ 加载状态、错误处理、离线降级完全实现
✅ Room 数据库集成完毕
✅ 聊天记录能正确保存和查询
✅ 所有单元测试通过
✅ 文档更新完毕
✅ 版本发布为 v2.0.0
```

**预期完成时间**：2-3 周（根据 API 配置复杂度）

---

**下一步**：选择 API 提供商，获取 API Key，开始任务 1 的实现。
