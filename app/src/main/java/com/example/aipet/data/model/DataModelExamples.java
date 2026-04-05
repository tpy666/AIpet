package com.example.aipet.data.model;

/**
 * 代码片段示例 - 复制粘贴使用
 * 此文件仅用于参考，不需要导入项目
 */

public class DataModelExamples {

    /**
     * ============ Pet 创建示例 ============
     */

    // 示例 1：无参构造
    private void example_pet_1() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("小白");
        pet.setSpecies("猫");
        pet.setPersonality("温和");
        pet.setSpeakingStyle("卖萌");
        pet.setAppearance("白色长毛");
        pet.setAvatar("http://example.com/avatar.png");
    }

    // 示例 2：快速创建
    private void example_pet_2() {
        Pet newPet = new Pet(
            "小橙",
            "狗",
            "调皮",
            "活泼",
            "橙色卷毛",
            "http://example.com/dog.png"
        );
    }

    // 示例 3：从数据库读取
    private void example_pet_3() {
        Pet dbPet = new Pet(
            1L,
            "小白",
            "猫",
            "温和",
            "卖萌",
            "白色长毛",
            "http://example.com/avatar.png"
        );
    }

    // 示例 4：访问属性
    private void example_pet_4() {
        Pet pet = new Pet("小白", "猫", "温和", "卖萌", "白色", "avatar.png");

        String name = pet.getName();
        String species = pet.getSpecies();
        String personality = pet.getPersonality();
        String speakingStyle = pet.getSpeakingStyle();
        String appearance = pet.getAppearance();
        String avatar = pet.getAvatar();

        System.out.println(pet.toString());
    }

    // 示例 5：修改属性
    private void example_pet_5() {
        Pet pet = new Pet("小白", "猫", "温和", "卖萌", "白色", "avatar.png");
        pet.setName("小白2.0");
        pet.setPersonality("活泼");
    }

    /**
     * ============ Message 创建示例 ============
     */

    // 示例 1：创建用户消息（快速）
    private void example_msg_1() {
        Message userMsg = new Message("user", "你好呀");
        // timestamp 自动设为当前时间
    }

    // 示例 2：创建 AI 消息（快速）
    private void example_msg_2() {
        Message aiMsg = new Message("assistant", "你好！很高兴见到你 😊");
    }

    // 示例 3：创建带宠物 ID 的消息
    private void example_msg_3() {
        long petId = 1L;
        Message msg = new Message("user", "你叫什么名字？", petId);
    }

    // 示例 4：从数据库读取
    private void example_msg_4() {
        Message dbMsg = new Message(
            5L,                           // id
            "user",                       // role
            "你今天开心吗？",             // content
            System.currentTimeMillis(),  // timestamp
            1L                           // petId
        );
    }

    // 示例 5：无参构造 + setter
    private void example_msg_5() {
        Message msg = new Message();
        msg.setId(10L);
        msg.setRole(Message.ROLE_USER);
        msg.setContent("你今天开心吗？");
        msg.setTimestamp(System.currentTimeMillis());
        msg.setPetId(1L);
    }

    // 示例 6：使用便利方法
    private void example_msg_6() {
        Message msg = new Message("user", "你好");

        if (msg.isFromUser()) {
            System.out.println("这是用户消息");
        }

        if (msg.isFromAssistant()) {
            System.out.println("这是 AI 消息");
        }
    }

    /**
     * ============ Activity 中的使用 ============
     */

    // 示例 7：CreatePetActivity 中创建宠物
    private void example_activity_create_pet() {
        // 获取输入
        // String name = etPetName.getText().toString().trim();
        // String desc = etPetDesc.getText().toString().trim();

        // 创建对象
        String name = "小白";
        String desc = "温和的白猫";

        Pet newPet = new Pet(
            name,
            "猫",
            desc,
            "温和、聪慧",
            "虚拟、可爱",
            ""
        );

        // TODO: 保存到数据库
        // petDao.insertPet(newPet);
    }

    // 示例 8：ChatActivity 中发送消息
    private void example_activity_chat_send() {
        // String content = etMessageInput.getText().toString().trim();
        String content = "你好呀";
        long currentPetId = 1L;

        // 创建用户消息
        Message userMessage = new Message(
            Message.ROLE_USER,
            content,
            currentPetId
        );

        // TODO: 保存用户消息到数据库
        // messageDao.insertMessage(userMessage);

        // TODO: 添加到聊天列表
        // chatAdapter.addMessage(userMessage);
    }

    // 示例 9：ChatActivity 中处理 AI 回复
    private void example_activity_chat_reply() {
        String aiContent = "你好！很高兴见到你！😊";
        long currentPetId = 1L;

        // 创建 AI 消息
        Message assistantMessage = new Message(
            Message.ROLE_ASSISTANT,
            aiContent,
            currentPetId
        );

        // TODO: 保存 AI 消息到数据库
        // messageDao.insertMessage(assistantMessage);

        // TODO: 添加到聊天列表
        // chatAdapter.addMessage(assistantMessage);
    }

    /**
     * ============ Adapter 中的使用 ============
     */

    // 示例 10：在 Adapter 中遍历消息
    /*
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.tvContent.setText(message.getContent());

        if (message.isFromUser()) {
            // 用户消息 - 显示在右侧
            holder.tvContent.setBackgroundColor(Color.BLUE);
            holder.tvContent.setTextColor(Color.WHITE);
        } else if (message.isFromAssistant()) {
            // AI 消息 - 显示在左侧
            holder.tvContent.setBackgroundColor(Color.GRAY);
            holder.tvContent.setTextColor(Color.BLACK);
        }

        // 显示时间
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeStr = sdf.format(new Date(message.getTimestamp()));
        holder.tvTime.setText(timeStr);
    }
    */

    /**
     * ============ Intent 传递数据 ============
     */

    // 示例 11：发送宠物对象
    /*
    Pet selectedPet = new Pet(1, "小白", "猫", "温和", "卖萌", "白色", "avatar.png");
    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
    intent.putExtra("pet", selectedPet);  // 自动序列化
    startActivity(intent);
    */

    // 示例 12：接收宠物对象
    /*
    Pet pet = (Pet) getIntent().getSerializableExtra("pet");
    if (pet != null) {
        String petName = pet.getName();
        long petId = pet.getId();
        // 使用 petId 加载该宠物的聊天记录
    }
    */

    /**
     * ============ 数据库操作预览 ============
     */

    // 示例 13：插入宠物
    /*
    Pet newPet = new Pet("小白", "猫", "温和", "卖萌", "白色", "avatar.png");
    long petId = petDao.insertPet(newPet);
    newPet.setId(petId);
    */

    // 示例 14：查询所有宠物
    /*
    List<Pet> allPets = petDao.getAllPets();
    for (Pet pet : allPets) {
        Log.d("PetList", pet.getName() + " - " + pet.getSpecies());
    }
    */

    // 示例 15：查询特定宠物的聊天记录
    /*
    long petId = 1L;
    List<Message> chatHistory = messageDao.getMessagesByPetId(petId);
    for (Message msg : chatHistory) {
        Log.d("Chat", msg.getRole() + ": " + msg.getContent());
    }
    */

    // 示例 16：删除消息
    /*
    Message oldMsg = chatHistory.get(0);
    messageDao.deleteMessage(oldMsg);
    */

    /**
     * ============ 验证和错误处理 ============
     */

    // 示例 17：验证宠物信息
    private boolean validatePet(Pet pet) {
        if (pet == null) return false;
        if (pet.getName() == null || pet.getName().trim().isEmpty()) return false;
        if (pet.getSpecies() == null || pet.getSpecies().trim().isEmpty()) return false;
        return true;
    }

    // 示例 18：验证消息
    private boolean validateMessage(Message msg) {
        if (msg == null) return false;
        if (msg.getContent() == null || msg.getContent().trim().isEmpty()) return false;
        if (!Message.ROLE_USER.equals(msg.getRole()) &&
            !Message.ROLE_ASSISTANT.equals(msg.getRole())) {
            return false;
        }
        return true;
    }

    /**
     * ============ 日志打印 ============
     */

    // 示例 19：调试宠物对象
    private void debugPet(Pet pet) {
        if (pet != null) {
            // Log.d("PetDebug", pet.toString());
            System.out.println("宠物信息: " + pet.toString());
        }
    }

    // 示例 20：调试消息对象
    private void debugMessage(Message msg) {
        if (msg != null) {
            // Log.d("MsgDebug", msg.toString());
            System.out.println("消息信息: " + msg.toString());
            System.out.println("来自: " + (msg.isFromUser() ? "用户" : "AI"));
        }
    }
}
