# Python 和本地服务器设置问题诊断

## 🔴 检测到的问题

你的系统中 Python 未正确安装或未添加到 PATH 中。

---

## ✅ 解决方案

### 方案 A: 使用自动化脚本（推荐）

如果你已经安装了 Python，但它不在 PATH 中：

1. **找到 Python 安装位置**
   - 通常在: `C:\Users\{YourUsername}\AppData\Local\Programs\Python\Python311`
   - 或: `C:\Program Files\Python311`

2. **修改批处理脚本**
   
   编辑 `run_server.bat`，在第一行添加 Python 路径：
   ```batch
   @echo off
   REM 修改这行为你的 Python 路径
   set PYTHON_PATH=C:\Users\{YourUsername}\AppData\Local\Programs\Python\Python311
   %PYTHON_PATH%\python.exe local_api_server.py
   ```

3. **运行脚本**
   ```
   双击 run_server.bat
   ```

### 方案 B: 安装 Python（如果未安装）

1. **下载 Python 3.11+**
   https://www.python.org/downloads/

2. **安装时重要步骤**
   - ✅ **勾选** "Add Python to PATH"
   - ✅ 选择安装位置（记住路径）
   - ✅ 完成安装

3. **验证安装**
   打开 PowerShell，输入：
   ```powershell
   python --version
   ```
   应该显示版本号，如: `Python 3.11.5`

4. **安装依赖**
   ```powershell
   python -m pip install Flask Flask-CORS
   ```

5. **启动服务器**
   ```powershell
   cd E:\Work\AIpet\test-backend
   python local_api_server.py
   ```

### 方案 C: 使用 WSL (Windows Subsystem for Linux)

如果你的 Windows 已启用 WSL：

```bash
cd /mnt/e/Work/AIpet/test-backend
python3 local_api_server.py
```

---

## 🔍 检查 Python 是否安装

### 在 PowerShell 中：
```powershell
# 检查是否安装了 Python
where python

# 如果没有输出，检查其他可能的位置
dir "C:\Users\$env:USERNAME\AppData\Local\Programs\Python" -Recurse -Filter python.exe
```

### 在 CMD 中：
```cmd
where python
python --version
```

---

## 📝 手动启动服务器的完整步骤

**如果以上都不行，尝试这个：**

1. **打开 PowerShell** (Win+X, 选择 PowerShell)

2. **找到 Python**
   ```powershell
   # 尝试这几个命令
   python --version
   python3 --version
   py --version
   ```

3. **如果找到了，启动服务器**
   ```powershell
   cd E:\Work\AIpet\test-backend
   
   # 替换 python 为你找到的命令
   python local_api_server.py
   ```

4. **看到以下输出表示成功**
   ```
   ==================================================
   🚀 Local AI Pet API Server 启动中...
   ==================================================
   📍 服务器运行在: http://0.0.0.0:8080
   📱 模拟器访问地址: http://10.0.2.2:8080
   ```

---

## 💡 一旦 Python 配置好，你可以这样快速启动

```powershell
# 只需运行一次配置后，以后每次都可以这样快速启动
cd E:\Work\AIpet\test-backend; python local_api_server.py
```

---

## 🎯 验证服务器是否工作

一旦服务器启动：

**在浏览器中打开** (本机):
```
http://localhost:8080
```

**预期看到**:
```json
{
  "name": "Local AI Pet API Server",
  "version": "1.0.0",
  "description": "本地测试 API 服务器...",
  ...
}
```

---

## 📞 需要帮助？

1. **查看 Python 官方指南**: https://www.python.org/downloads/windows/
2. **查看这篇 Windows PATH 指南**: https://docs.python.org/3/using/windows.html
3. **检查防火墙**: 确保 Windows 防火墙允许 Python 访问网络

---

**完成配置后，回到应用进行测试！** ✅
