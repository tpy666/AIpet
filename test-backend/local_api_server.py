#!/usr/bin/env python3
"""
本地 AI 宠物 API 测试服务器
支持模拟 OpenAI 风格的 API 请求
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import logging
from datetime import datetime

# 设置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# 宠物类型对应的回复模板
PET_RESPONSES = {
    "cat": [
        "喵~我好像不太感兴趣呢，但既然你问了，我告诉你吧...",
        "喵喵~这个问题有点复杂，但让我试试...",
        "好的，我来给你解释一下...",
        "喵~我猜你想知道这个...",
    ],
    "dog": [
        "汪汪！这太让人兴奋了！",
        "汪！我超喜欢这个话题！",
        "汪汪汪~让我告诉你...",
        "嗯，我认为...",
    ],
    "rabbit": [
        "呱吱~这是个好问题...",
        "嘻~我想到了一个答案...",
        "嗯嗯，让我想想...",
        "呱~这很有趣...",
    ],
    "bird": [
        "啾啾！这是个好想法！",
        "啾~我喜欢这个...",
        "叽叽喳喳~让我说说...",
        "啾啾啾~我的想法是...",
    ],
}

# 默认回复
DEFAULT_RESPONSES = [
    "这是一个很好的问题！根据我的理解...",
    "我非常赞同你的想法。让我详细解释一下...",
    "这是我对这个话题的看法...",
    "有趣的问题。我来告诉你我的想法...",
]

def get_pet_response(pet_type, user_message, system_prompt=None):
    """根据宠物类型和系统提示词生成个性化响应"""
    import random
    
    pet_type = pet_type.lower() if pet_type else "cat"
    
    # 系统提示词的分析和提取
    is_cute = False
    is_gentle = False
    is_playful = False
    is_cold = False
    is_energetic = False
    pet_name = "小伙伴"
    
    if system_prompt and system_prompt.strip():
        logger.info(f"分析系统提示词...")
        # 从系统提示词中提取宠物信息和性格特征
        if '卖萌' in system_prompt or '可爱' in system_prompt:
            is_cute = True
        if '温柔' in system_prompt or '友善' in system_prompt:
            is_gentle = True
        if '调皮' in system_prompt or '活泼' in system_prompt or '顽皮' in system_prompt:
            is_playful = True
        if '高冷' in system_prompt or '冷漠' in system_prompt:
            is_cold = True
        if '热情' in system_prompt or '兴奋' in system_prompt or '热烈' in system_prompt:
            is_energetic = True
        
        # 提取宠物名字
        if '名叫' in system_prompt:
            parts = system_prompt.split('名叫')
            if len(parts) > 1:
                name_part = parts[1].split('的')[0].strip()
                pet_name = name_part
    
    # 基于性格特征生成动态回复
    if is_cute:
        openers = [
            "呜~",
            "嘻嘻~",
            "萌萌哒~",
            "撒娇地说~",
        ]
        responses_templates = [
            "你说的'{}'，我好感兴趣呢！让我来跟你分享一下我的想法~",
            "关于'{}'这个话题，我家有不同的看法呢~仔细听哦！",
            "哈！'{}'这个问题触及我的兴趣点了！",
            "'{}'对吧？我也这么觉得！不过...",
            "嘻嘻，你问我'{}'，我有好多话想说~",
        ]
    elif is_gentle:
        openers = [
            "温柔地说~",
            "笑着说~",
            "轻声说~",
        ]
        responses_templates = [
            "关于你提到的'{}'，我有一些想法。希望对你有帮助。",
            "'{}'是个很温暖的话题。让我慢慢跟你说。",
            "我理解你说的'{}'。根据我的经验...",
            "'{}'让我想起了很多事。听我说吧。",
        ]
    elif is_playful:
        openers = [
            "调皮地笑~",
            "兴奋地说~",
            "跳跳跳~",
        ]
        responses_templates = [
            "哈哈！'{}'这个太有趣了！我来告诉你...",
            "'}' 对不对？这真的超级刺激！",
            "你说的'{}'，让我想到一个疯狂的主意！",
            "'{}'？这不正是我想说的吗！",
            "呼~你居然也问起'{}'来了！我太兴奋了！",
        ]
    elif is_cold:
        openers = [
            "不在乎地说~",
            "冷淡地回答~",
            "漠然地说~",
        ]
        responses_templates = [
            "关于'{}'...哼，我有不同的看法。",
            "'{}'？这不过是小事罢了。",
            "你问我'{}'？我的观点很简单...",
            "'{}'...我不太感兴趣，但既然你非要我说...",
        ]
    elif is_energetic:
        openers = [
            "热情洋溢地说~",
            "兴高采烈地说~",
            "激动地说~",
        ]
        responses_templates = [
            "天哪！'{}'太棒了！我超级喜欢这个话题！",
            "'{}'？是的是的是的！这真的太令人兴奋了！",
            "哇哦！你竟然问我'{}'！我有这么多想说！",
            "'{}'绝对是个超级话题！让我极力推荐...",
        ]
    else:
        openers = [
            "说道~",
            "告诉你~",
            "解释说~",
        ]
        responses_templates = [
            "关于'{}'，我的看法是...",
            "'{}'这个问题，我来给你分析一下...",
            "你问我'{}'？让我慢慢跟你说。",
            "关于'{}'，我有这样一个想法...",
        ]
    
    # 生成回复
    opener = random.choice(openers) if openers else ""
    template = random.choice(responses_templates) if responses_templates else "关于'{}'，我有想法..."
    
    # 获取用户消息的前10个字符作为话题
    topic = user_message[:15] if len(user_message) > 15 else user_message
    
    # 添加宠物特色回应
    pet_specific_endings = []
    if pet_type == "cat":
        pet_specific_endings = [
            "喵~",
            "喵喵，就是这样。",
            "汇聚所有的猫咪智慧，我的答案就是如此。",
        ]
    elif pet_type == "dog":
        pet_specific_endings = [
            "汪~",
            "汪汪，真的就是这样！",
            "作为一只忠诚的狗狗，我保证这是真的！",
        ]
    elif pet_type == "rabbit":
        pet_specific_endings = [
            "呱吱~",
            "嗯哼，就是我的看法。",
            "作为一只聪慧的兔子，我以此为荣。",
        ]
    elif pet_type == "bird":
        pet_specific_endings = [
            "啾啾~",
            "唧唧喳喳，我的意见就是这样。",
            "从高空俯视，我的观点就是如此。",
        ]
    else:
        pet_specific_endings = [
            "~",
            "就是这样。",
            "我的最终答案。",
        ]
    
    ending = random.choice(pet_specific_endings) if pet_specific_endings else "~"
    
    # 组合最终回复
    response = f"{opener}\n{template.format(topic)}\n\n...{ending}"
    
    return response

@app.route('/api/chat', methods=['POST'])
def chat():
    """处理聊天请求 - 支持应用自定义格式和 OpenAI 格式"""
    try:
        data = request.get_json()
        logger.info(f"收到请求: {data}")
        
        # 提取请求数据 - 支持应用自定义格式
        # 检查是否是应用格式 (message + petInfo)
        if 'message' in data and 'petInfo' in data:
            user_message = data.get('message', '')
            pet_info = data.get('petInfo', {})
            pet_type = pet_info.get('species', 'cat').lower()
            pet_name = pet_info.get('name', 'AIpet')
            pet_personality = pet_info.get('personality', '')
            pet_speaking_style = pet_info.get('speakingStyle', '')
            system_prompt = data.get('systemPrompt', '')
            model = 'local-pet-chat'
        else:
            # OpenAI 格式
            messages = data.get('messages', [])
            model = data.get('model', 'gpt-3.5-turbo')
            pet_type = data.get('pet_type', 'cat')
            pet_name = data.get('pet_name', 'AIpet')
            pet_personality = data.get('pet_personality', '')
            pet_speaking_style = data.get('pet_speaking_style', '')
            system_prompt = data.get('system_prompt', '')
            
            # 获取最后一条用户消息
            user_message = ""
            if messages:
                for msg in reversed(messages):
                    if msg.get('role') == 'user':
                        user_message = msg.get('content', '')
                        break
        
        if not user_message:
            return jsonify({"error": "No user message provided"}), 400
        
        logger.info(f"宠物类型: {pet_type}, 宠物名称: {pet_name}, 性格: {pet_personality}, 说话风格: {pet_speaking_style}")
        if system_prompt:
            logger.info(f"系统提示词收到: {system_prompt[:100]}...")
        
        # 生成对话回复
        reply = get_pet_response(pet_type, user_message, system_prompt)
        
        # 构建响应 - 支持应用自定义格式
        response = {
            "id": "chatcmpl-local-test",
            "object": "text_completion",
            "created": int(datetime.now().timestamp()),
            "model": model,
            "choices": [
                {
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": reply
                    },
                    "finish_reason": "stop"
                }
            ],
            "usage": {
                "prompt_tokens": len(user_message.split()),
                "completion_tokens": len(reply.split()),
                "total_tokens": len(user_message.split()) + len(reply.split())
            }
        }
        
        logger.info(f"返回响应: {response['choices'][0]['message']['content'][:50]}...")
        return jsonify(response), 200
        
    except Exception as e:
        logger.error(f"错误: {str(e)}", exc_info=True)
        return jsonify({"error": str(e)}), 500

@app.route('/api/health', methods=['GET'])
def health():
    """健康检查端点"""
    return jsonify({"status": "ok", "message": "Local API server is running"}), 200

@app.route('/', methods=['GET'])
def home():
    """主页"""
    return jsonify({
        "name": "Local AI Pet API Server",
        "version": "1.0.0",
        "description": "本地测试 API 服务器，支持 AIpet 应用的离线测试",
        "endpoints": {
            "POST /api/chat": "发送聊天消息",
            "GET /api/health": "健康检查",
            "GET /": "关于"
        }
    }), 200

if __name__ == '__main__':
    print("=" * 60)
    print("🚀 Local AI Pet API Server 启动中...")
    print("=" * 60)
    print("📍 服务器运行在: http://0.0.0.0:8080")
    print("📱 模拟器访问地址: http://10.0.2.2:8080")
    print("💡 API 端点: /api/chat, /api/health, /")
    print("⌨️  按 Ctrl+C 停止服务器")
    print("=" * 60)
    print()
    
    app.run(host='0.0.0.0', port=8080, debug=False)
