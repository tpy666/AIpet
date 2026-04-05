#!/bin/bash

# Mac/Linux 启动脚本：本地 API 测试服务器

echo ""
echo "=================================================="
echo "  Local AI Pet API Server - 启动脚本"
echo "=================================================="
echo ""

# 检查 Python
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到 Python 3"
    echo "请安装 Python 3.8+ : https://www.python.org/downloads/"
    exit 1
fi

echo "✓ 已检测到 Python"
python3 --version

# 检查并安装依赖
echo ""
echo "正在检查依赖..."
if ! python3 -c "import flask" 2>/dev/null; then
    echo "正在安装依赖库 (Flask, Flask-CORS)..."
    pip3 install -r requirements.txt
    if [ $? -ne 0 ]; then
        echo "错误: 依赖安装失败"
        exit 1
    fi
    echo "✓ 依赖安装完成"
else
    echo "✓ 依赖已安装"
fi

# 启动服务器
echo ""
echo "=================================================="
echo "  启动 API 服务器..."
echo "=================================================="
echo ""

python3 local_api_server.py
