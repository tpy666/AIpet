# 📦 数据模型实现总结

## ✅ 已完成的工作

### 1. Pet.java - 宠物角色模型类

📁 路径: `app/src/main/java/com/example/aipet/data/model/Pet.java`

**属性（7 个）:**
```java
long id                 // 宠物 ID（主键）
String name             // 宠物名称
String species          // 物种
String personality      // 性格描述
String speakingStyle    // 说话风格
String appearance       // 外观描述
String avatar           // 头像 URL 或路径
```

**构造方法（3 个）:**
```java
Pet()                                    // 无参构造
Pet(String name, String species, ...)   // 新建宠物（推荐）
Pet(long id, String name, ...)          // 从数据库读取
```

**特性:**
- ✅ 包含完整的 getter/setter
- ✅ 实现 Serializable 接口（支持 Intent 传递）
- ✅ 包含 toString() 方法
- ✅ 包含 equals() 和 hashCode() 方法（基于 ID）
- ✅ Javadoc 文档注释

---

### 2. Message.java - 聊天消息模型类

📁 路径: `app/src/main/java/com/example/aipet/data/model/Message.java`

**属性（5 个）:**
```java
long id                 // 消息 ID（主键）
String role             // 消息角色（user 或 assistant）
String content          // 消息内容
long timestamp          // 时间戳（毫秒）
long petId              // 关联的宠物 ID
```

**常量（2 个）:**
```java
static final String ROLE_USER = "user"              // 用户消息
static final String ROLE_ASSISTANT = "assistant"   // AI 助手消息
```

**构造方法（4 个）:**
```java
Message()                                      // 无参构造
Message(String role, String content)           // 快速创建
Message(String role, String content, long petId) // 带宠物 ID
Message(long id, String role, ...)             // 从数据库读取
```

**便利方法（2 个）:**
```java
boolean isFromUser()        // 判断是否用户消息
boolean isFromAssistant()   // 判断是否 AI 消息
```

**特性:**
- ✅ 包含完整的 getter/setter
- ✅ 实现 Serializable 接口
- ✅ 时间戳自动设置为当前时间
- ✅ 包含便利判断方法
- ✅ 包含 toString()、equals()、hashCode()
- ✅ Javadoc 文档注释

---

### 3. DataModelExamples.java - 代码示例

📁 路径: `app/src/main/java/com/example/aipet/data/model/DataModelExamples.java`

**包含 20+ 个实战代码示例:**
- Pet 创建示例 (5 个)
- Message 创建示例 (6 个)
- Activity 中的使用 (3 个)
- Adapter 中的使用 (1 个)
- Intent 传递数据 (2 个)
- 数据库操作预览 (4 个)
- 验证和错误处理 (2 个)
- 日志打印示例 (2 个)

---

## 📚 生成的文档

### 1. PROJECT_FRAMEWORK.md
总体项目框架文档，包含：
- 项目目录结构
- Activity 注册配置
- 基础依赖配置
- 页面设计说明
- 下一步开发指南

### 2. DATA_MODEL_GUIDE.md
完整的数据模型使用指南，包含：
- 模型类概览
- 详细使用示例
- Activity 中的实际应用
- Adapter 中的使用
- Room 数据库集成预览
- 最佳实践
- 约 8.5 KB

### 3. MODEL_QUICK_REF.md
快速参考卡，包含：
- 属性对比表
- 构造方法速查
- 常用方法速查
- 实战代码片段
- 数据库集成预览
- 检查清单
- 约 4.7 KB

---

## 🎯 快速使用指南

### 在 CreatePetActivity 中创建宠物

```java
private void createPet() {
    String name = etPetName.getText().toString().trim();
    String desc = etPetDesc.getText().toString().trim();

    if (name.isEmpty()) {
        Toast.makeText(this, "请输入角色名称", Toast.LENGTH_SHORT).show();
        return;
    }

    // 创建 Pet 对象
    Pet newPet = new Pet(
        name,
        "AI宠物",
        desc,
        "温和、聪慧",
        "虚拟、可爱",
        ""
    );

    // TODO: 保存到数据库
    // long petId = petDao.insertPet(newPet);

    Toast.makeText(this, "角色创建成功！", Toast.LENGTH_SHORT).show();
    finish();
}
```

### 在 ChatActivity 中处理消息

```java
private void sendMessage() {
    String content = etMessageInput.getText().toString().trim();

    if (content.isEmpty()) {
        Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show();
        return;
    }

    // 创建用户消息
    Message userMessage = new Message(
        Message.ROLE_USER,
        content,
        currentPetId
    );

    // 添加到列表
    // chatAdapter.addMessage(userMessage);

    // 清空输入框
    etMessageInput.setText("");

    // TODO: 调用 AI 接口
    // callAiApi(content, (response) -> {
    //     Message aiMsg = new Message(Message.ROLE_ASSISTANT, response, currentPetId);
    //     chatAdapter.addMessage(aiMsg);
    // });
}
```

### 在 Adapter 中遍历消息

```java
public void onBindViewHolder(ViewHolder holder, int position) {
    Message message = messages.get(position);

    holder.tvContent.setText(message.getContent());

    if (message.isFromUser()) {
        holder.tvContent.setBackgroundColor(Color.BLUE);  // 用户消息 - 右侧
    } else {
        holder.tvContent.setBackgroundColor(Color.GRAY);  // AI 消息 - 左侧
    }

    // 显示时间戳
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    holder.tvTime.setText(sdf.format(new Date(message.getTimestamp())));
}
```

---

## 🗂️ 项目结构更新

```
E:/Work/AIpet/
├── app/src/main/java/com/example/aipet/
│   └── data/
│       └── model/
│           ├── Pet.java                 ✨ 新增
│           ├── Message.java             ✨ 新增
│           └── DataModelExamples.java   ✨ 新增 (示例代码)
│
└── 文档文件
    ├── PROJECT_FRAMEWORK.md             📋 项目框架
    ├── DATA_MODEL_GUIDE.md              📖 使用指南
    └── MODEL_QUICK_REF.md               ⚡ 快速参考
```

---

## 🔄 与其他组件的集成

### 与 Activity 的集成

```java
// CreatePetActivity
Pet pet = new Pet(name, species, personality, speakingStyle, appearance, avatar);

// ChatActivity
Message msg = new Message(role, content, petId);
```

### 与 Intent 的集成

```java
// 发送端
intent.putExtra("pet", selectedPet);

// 接收端
Pet pet = (Pet) getIntent().getSerializableExtra("pet");
```

### 与 Adapter 的集成

```java
// PetCardListAdapter 中
List<Pet> petList = ...;  // 从数据库加载

// ChatMessageAdapter 中
List<Message> messages = ...;  // 按 petId 加载
adapter.addMessage(newMessage);
```

### 与数据库的集成（后续）

```java
// PetDao
@Query("SELECT * FROM pets")
List<Pet> getAllPets();

// MessageDao
@Query("SELECT * FROM messages WHERE petId = :petId")
List<Message> getMessagesByPetId(long petId);
```

---

## 📊 数据模型特性总结

| 特性 | Pet | Message | 说明 |
|------|-----|---------|------|
| Serializable | ✅ | ✅ | 支持 Intent 传递 |
| 无参构造 | ✅ | ✅ | 灵活创建 |
| 完整构造 | ✅ | ✅ | 便利初始化 |
| Getter/Setter | ✅ | ✅ | 数据访问 |
| toString() | ✅ | ✅ | 调试打印 |
| equals() | ✅ | ✅ | 对象比较 |
| hashCode() | ✅ | ✅ | HashMap 支持 |
| 常量定义 | ❌ | ✅ | role 常量 |
| 便利方法 | ❌ | ✅ | isFromUser() |
| Javadoc | ✅ | ✅ | 文档注释 |

---

## ⚠️ 注意事项

1. **序列化版本号**: `serialVersionUID = 1L`
   - 修改类结构时应更新此值
   - 避免旧版本数据反序列化错误

2. **时间戳**: Message 的 timestamp 自动设为当前时间
   - 可通过 setTimestamp() 覆盖
   - 用于消息排序和显示

3. **Null 安全**: 建议在使用前检查 null
   ```java
   if (pet != null && pet.getName() != null) {
       String name = pet.getName();
   }
   ```

4. **ID 管理**: 数据库插入后返回自动生成的 ID
   ```java
   long newId = petDao.insertPet(pet);
   pet.setId(newId);
   ```

---

## 🚀 下一步计划

### 第一阶段：数据持久化
- [ ] 创建 PetDao 接口
- [ ] 创建 MessageDao 接口
- [ ] 创建 AppDatabase 类
- [ ] 集成 Room 依赖

### 第二阶段：UI 适配器
- [ ] 创建 PetCardListAdapter
- [ ] 创建 ChatMessageAdapter
- [ ] 实现列表展示和交互

### 第三阶段：网络通信
- [ ] 创建 API 服务接口
- [ ] 实现聊天 API 调用
- [ ] 处理网络请求和响应

### 第四阶段：业务逻辑
- [ ] 创建 Repository 类
- [ ] 创建 ViewModel 类
- [ ] 实现数据和 UI 分离

---

## ✨ 最佳实践检查表

- ✅ 遵循 Java 命名规范（PascalCase 类名、camelCase 方法）
- ✅ 包含 Javadoc 注释
- ✅ 实现 Serializable 接口
- ✅ 提供无参构造方法（数据库兼容）
- ✅ 提供便利构造方法（快速创建）
- ✅ 包含 getter/setter 方法
- ✅ 包含 toString/equals/hashCode
- ✅ 常量定义（Message.ROLE_*）
- ✅ 便利判断方法（isFromUser/isFromAssistant）
- ✅ 时间戳自动设置

---

## 📞 问题排查

### 编译错误："cannot find symbol: class Pet"
解决方案：
```
1. 检查 import 语句: import com.example.aipet.data.model.Pet;
2. 检查文件路径是否正确
3. 清理并重建项目: Build > Clean Project
```

### Intent 传递数据为 null
解决方案：
```
1. 确保实现 Serializable 接口
2. 使用 putExtra(key, object) 传递
3. 使用 getSerializableExtra(key) 接收
```

### 数据库无法保存消息
解决方案：
- 等待 Room DAO 创建完毕
- 检查数据库权限
- 使用后台线程执行数据库操作

---

## 📖 相关文档

| 文档 | 用途 | 大小 |
|------|------|------|
| PROJECT_FRAMEWORK.md | 项目整体框架 | 9.8 KB |
| DATA_MODEL_GUIDE.md | 详细使用指南 | 8.5 KB |
| MODEL_QUICK_REF.md | 快速参考卡 | 4.7 KB |
| DataModelExamples.java | 代码示例 | 参考文件 |

---

**完成时间**: 2026-03-31
**Java 版本**: 11+
**Android 最低版本**: API 33
**数据库**: 已准备好集成 Room ORM
