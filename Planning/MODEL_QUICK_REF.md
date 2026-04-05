# 数据模型 - 快速参考卡

## 📋 Pet.java - 宠物角色模型

| 属性 | 类型 | 说明 |
|------|------|------|
| id | long | 宠物 ID（主键） |
| name | String | 宠物名称 |
| species | String | 物种（如：猫、狗、兔子等） |
| personality | String | 性格描述 |
| speakingStyle | String | 说话风格 |
| appearance | String | 外观描述 |
| avatar | String | 头像 URL 或本地路径 |

### 构造方法

```java
// 1️⃣ 无参构造
Pet pet = new Pet();

// 2️⃣ 新建宠物（推荐）
Pet pet = new Pet(name, species, personality, speakingStyle, appearance, avatar);

// 3️⃣ 从数据库读取
Pet pet = new Pet(id, name, species, personality, speakingStyle, appearance, avatar);
```

### 常用方法

```java
pet.getName()                    // 获取宠物名称
pet.setName("新名字")            // 设置宠物名称
pet.toString()                   // 字符串表示
pet.equals(otherPet)             // 比较 ID
```

---

## 💬 Message.java - 聊天消息模型

| 属性 | 类型 | 说明 |
|------|------|------|
| id | long | 消息 ID（主键） |
| role | String | 消息角色（"user" 或 "assistant"） |
| content | String | 消息内容 |
| timestamp | long | 时间戳（毫秒） |
| petId | long | 关联的宠物 ID |

### 常量

```java
Message.ROLE_USER           // "user"
Message.ROLE_ASSISTANT      // "assistant"
```

### 构造方法

```java
// 1️⃣ 快速创建用户消息
Message msg = new Message(role, content);  // timestamp 自动设为当前时间

// 2️⃣ 创建带宠物 ID 的消息
Message msg = new Message(role, content, petId);

// 3️⃣ 从数据库读取
Message msg = new Message(id, role, content, timestamp, petId);

// 4️⃣ 无参构造 + setter
Message msg = new Message();
msg.setRole("user");
msg.setContent("你好");
```

### 常用方法

```java
msg.getContent()                // 获取消息内容
msg.isFromUser()               // 判断是否用户消息（返回 boolean）
msg.isFromAssistant()          // 判断是否 AI 消息（返回 boolean）
msg.getTimestamp()             // 获取时间戳
msg.toString()                 // 字符串表示
```

---

## 🚀 实战代码片段

### 在 CreatePetActivity 中创建宠物

```java
// 获取用户输入
String name = etPetName.getText().toString().trim();
String desc = etPetDesc.getText().toString().trim();

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
// petDao.insertPet(newPet);
```

### 在 ChatActivity 中处理消息

```java
// 用户发送消息
String userText = etMessageInput.getText().toString().trim();
Message userMsg = new Message(Message.ROLE_USER, userText, currentPetId);

// AI 回复消息
Message aiMsg = new Message(Message.ROLE_ASSISTANT, "你好呀！", currentPetId);

// 判断消息来源
if (userMsg.isFromUser()) {
    // 显示在右侧气泡
}

if (aiMsg.isFromAssistant()) {
    // 显示在左侧气泡
}
```

### 在 Adapter 中遍历消息

```java
public void onBindViewHolder(ViewHolder holder, int position) {
    Message message = messages.get(position);

    holder.tvContent.setText(message.getContent());

    if (message.isFromUser()) {
        holder.tvContent.setBackgroundColor(Color.BLUE);
    } else {
        holder.tvContent.setBackgroundColor(Color.GRAY);
    }
}
```

---

## 🔗 与 Intent 传递数据

```java
// 发送 Activity
Pet selectedPet = ...;
Intent intent = new Intent(this, ChatActivity.class);
intent.putExtra("pet", selectedPet);  // 自动序列化
startActivity(intent);

// 接收 Activity
Pet pet = (Pet) getIntent().getSerializableExtra("pet");
if (pet != null) {
    String petName = pet.getName();
}
```

---

## 📦 数据库集成预览

> 后续将使用 Room ORM 自动映射这些模型

```java
@Dao
public interface PetDao {
    @Insert
    long insertPet(Pet pet);

    @Query("SELECT * FROM pets")
    List<Pet> getAllPets();
}

@Dao
public interface MessageDao {
    @Insert
    long insertMessage(Message message);

    @Query("SELECT * FROM messages WHERE petId = :petId ORDER BY timestamp DESC")
    List<Message> getMessagesByPetId(long petId);
}
```

---

## ✅ 检查清单

- ✅ Pet.java：7 属性 + 3 构造 + getter/setter + toString/equals/hashCode
- ✅ Message.java：5 属性 + 4 构造 + 便利方法 + 常量定义
- ✅ 两个类都实现 Serializable（支持 Intent 传递）
- ✅ 包含 Javadoc 注释
- ✅ 遵循 Java 命名规范
- ✅ 已生成完整使用指南

下一步建议：
1. 创建 PetDao 和 MessageDao 接口
2. 创建 AppDatabase 类
3. 实现本地数据持久化
