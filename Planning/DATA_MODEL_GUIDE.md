# 数据模型类使用指南

## 📦 模型类概览

### Pet.java - 宠物角色模型
```
属性:
  - id: long              宠物 ID（主键）
  - name: String          宠物名称
  - species: String       物种
  - personality: String   性格描述
  - speakingStyle: String 说话风格
  - appearance: String    外观描述
  - avatar: String        头像 URL 或路径
```

### Message.java - 聊天消息模型
```
属性:
  - id: long              消息 ID（主键）
  - role: String          消息角色（user 或 assistant）
  - content: String       消息内容
  - timestamp: long       时间戳（毫秒）
  - petId: long           关联宠物 ID

常量:
  - ROLE_USER = "user"              用户消息
  - ROLE_ASSISTANT = "assistant"   AI 助手消息
```

---

## 💡 使用示例

### 创建宠物对象

```java
// 方式 1：无参构造，然后使用 setter
Pet pet = new Pet();
pet.setId(1);
pet.setName("小白");
pet.setSpecies("猫");
pet.setPersonality("温和、慵懒");
pet.setSpeakingStyle("卖萌、撒娇");
pet.setAppearance("白色、长毛");
pet.setAvatar("https://example.com/avatar1.png");

// 方式 2：使用不含 ID 的构造方法（推荐新建）
Pet newPet = new Pet(
    "小橙",
    "狗",
    "调皮、热情",
    "活泼、搞笑",
    "橙色、卷毛",
    "https://example.com/avatar2.png"
);

// 方式 3：使用完整构造方法（从数据库读取）
Pet dbPet = new Pet(
    1L,
    "小白",
    "猫",
    "温和、慵懒",
    "卖萌、撒娇",
    "白色、长毛",
    "https://example.com/avatar1.png"
);

// 访问属性
System.out.println(pet.getName());        // "小白"
System.out.println(newPet.toString());    // 打印完整信息
```

### 创建消息对象

```java
// 方式 1：快速创建用户消息
Message userMsg = new Message("user", "你好呀");
// 自动设置 timestamp 为当前时间

// 方式 2：创建 AI 回复消息
Message assistantMsg = new Message(
    "assistant",
    "你好！很高兴见到你 😊",
    1L  // petId
);

// 方式 3：从数据库读取消息
Message dbMsg = new Message(
    5L,                               // id
    "user",                           // role
    "你叫什么名字？",                 // content
    System.currentTimeMillis(),      // timestamp
    1L                               // petId
);

// 方式 4：使用 setter
Message msg = new Message();
msg.setId(10L);
msg.setRole(Message.ROLE_USER);
msg.setContent("你今天开心吗？");
msg.setTimestamp(System.currentTimeMillis());
msg.setPetId(1L);

// 便利方法
System.out.println(userMsg.isFromUser());       // true
System.out.println(assistantMsg.isFromAssistant()); // true
System.out.println(msg.getRole());              // "user"
```

### 在 Activity 中的实际应用

```java
// CreatePetActivity.java
private void createPet() {
    String name = etPetName.getText().toString().trim();
    String desc = etPetDesc.getText().toString().trim();

    if (name.isEmpty()) {
        Toast.makeText(this, "请输入角色名称", Toast.LENGTH_SHORT).show();
        return;
    }

    // 创建 Pet 对象
    Pet newPet = new Pet(
        name,                              // 名称
        "AI宠物",                         // 物种
        desc,                             // 性格就用描述替代
        "温和、聪慧",                    // 说话风格
        "虚拟、可爱",                    // 外观
        ""                                // avatar（可后续添加）
    );

    // TODO: 保存到数据库
    // petDatabase.insertPet(newPet);

    Toast.makeText(this, "角色创建成功！", Toast.LENGTH_SHORT).show();
    finish();
}

// ChatActivity.java
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
        currentPetId    // 当前聊天的宠物 ID
    );

    // 保存用户消息
    // messageDatabase.insertMessage(userMessage);

    // 添加到聊天列表
    // chatAdapter.addMessage(userMessage);

    // 清空输入框
    etMessageInput.setText("");

    // TODO: 调用 AI 接口获取回复
    // callAiApi(content, (response) -> {
    //     Message assistantMessage = new Message(
    //         Message.ROLE_ASSISTANT,
    //         response,
    //         currentPetId
    //     );
    //     messageDatabase.insertMessage(assistantMessage);
    //     chatAdapter.addMessage(assistantMessage);
    // });
}
```

### 在 Adapter 中的使用

```java
// ChatMessageAdapter.java
public class ChatMessageAdapter extends BaseAdapter {
    private List<Message> messages;

    public ChatMessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messages.get(position);

        if (message.isFromUser()) {
            // 显示用户消息（右侧气泡）
            // ...
        } else if (message.isFromAssistant()) {
            // 显示 AI 消息（左侧气泡）
            // ...
        }

        return convertView;
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }
}
```

---

## 🗄️ 与 Room 数据库集成

### 后续将创建的 DAO 接口

```java
// PetDao.java
@Dao
public interface PetDao {
    @Insert
    long insertPet(Pet pet);

    @Update
    void updatePet(Pet pet);

    @Delete
    void deletePet(Pet pet);

    @Query("SELECT * FROM pets WHERE id = :petId")
    Pet getPetById(long petId);

    @Query("SELECT * FROM pets")
    List<Pet> getAllPets();
}

// MessageDao.java
@Dao
public interface MessageDao {
    @Insert
    long insertMessage(Message message);

    @Query("SELECT * FROM messages WHERE petId = :petId ORDER BY timestamp DESC")
    List<Message> getMessagesByPetId(long petId);

    @Delete
    void deleteMessage(Message message);
}
```

### Room 实体注解示例（可选增强）

若要使用 Room 数据库，可以添加注解：

```java
@Entity(tableName = "pets")
public class Pet implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;
    // ...
}

@Entity(tableName = "messages",
        foreignKeys = @ForeignKey(entity = Pet.class,
                                 parentColumns = "id",
                                 childColumns = "petId"))
public class Message implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "role")
    private String role;

    @ColumnInfo(name = "content")
    private String content;
    // ...
}
```

---

## ✨ 最佳实践

### 1️⃣ 验证数据

```java
// 在 setter 中添加验证
public void setName(String name) {
    if (name != null && !name.trim().isEmpty()) {
        this.name = name.trim();
    }
}
```

### 2️⃣ 使用常量

```java
// 对于消息角色，总是使用常量
if (message.getRole().equals(Message.ROLE_USER)) {
    // 处理用户消息
}

// 不要硬编码字符串
if (message.getRole().equals("user")) {  // ❌ 避免
    // ...
}
```

### 3️⃣ Null 检查

```java
// 在使用对象前检查 null
if (pet != null && pet.getName() != null) {
    String displayName = pet.getName();
}
```

### 4️⃣ 序列化

```java
// Pet 和 Message 都实现了 Serializable
// 可以在 Intent 中传递
Pet selectedPet = new Pet(1L, "小白", ...);
Intent intent = new Intent(this, ChatActivity.class);
intent.putExtra("pet", selectedPet);

// 在接收 Activity 中
Pet pet = (Pet) getIntent().getSerializableExtra("pet");
```

---

## 📝 总结

- ✅ Pet.java：完整的宠物数据模型（7 个属性 + 3 个构造方法 + getter/setter）
- ✅ Message.java：完整的消息数据模型（5 个属性 + 4 个构造方法 + 便利方法）
- ✅ 支持序列化，可在 Intent 中传递
- ✅ 包含 toString/equals/hashCode 方法
- ✅ 常量定义便于使用
- ✅ 已准备好集成 Room 数据库

下一步可以：
1. 创建 Room DAO 接口
2. 创建 AppDatabase 类
3. 创建 Repository 进行数据操作
4. 在 Activity 中使用这些模型类
