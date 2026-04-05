@echo off
REM Windows 启动脚本：本地 API 测试服务器
REM 
REM 使用说明：
REM 1. 双击此文件运行
REM 2. 首次运行会自动安装依赖 (Flask, Flask-CORS)
REM 3. 服务器启动后，模拟器访问 http://10.0.2.2:8080

setlocal enabledelayedexpansion

echo.
echo ==================================================
echo  Local AI Pet API Server - 启动脚本
echo ==================================================
echo.

REM 检查 Python 是否安装
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 系统未检测到 Python
    echo 请下载并安装 Python 3.8+: https://www.python.org/downloads/
    echo.
    pause
    exit /b 1
)

echo ✓ 已检测到 Python
python --version

REM 检查并安装依赖
echo.
echo 正在检查依赖...
pip show flask >nul 2>&1
if %errorlevel% neq 0 (
    echo 正在安装依赖库 (Flask, Flask-CORS)...
    pip install -r requirements.txt
    if %errorlevel% neq 0 (
        echo 错误: 依赖安装失败
        pause
        exit /b 1
    )
    echo ✓ 依赖安装完成
) else (
    echo ✓ 依赖已安装
)

REM 启动服务器
echo.
echo ==================================================
echo  启动 API 服务器...
echo ==================================================
echo.
python local_api_server.py

if %errorlevel% neq 0 (
    echo.
    echo 错误: 服务器启动失败
    pause
)
