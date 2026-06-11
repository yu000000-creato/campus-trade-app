# 项目运行指南

## ⚠️ 当前状态

系统检查结果：
- ❌ Maven 未安装（后端 Spring Boot 需要）
- ✅ Gradle 已安装（Android 项目需要）
- ❓ Android Studio 未确认
- ❓ MySQL 未确认

## 📋 需要安装的工具

### 1. Maven（必需）

**下载地址：** https://maven.apache.org/download.cgi

**安装步骤：**
1. 下载 `apache-maven-3.9.x-bin.zip`
2. 解压到 `C:\Program Files\Apache\maven`
3. 配置环境变量：
   - 新建系统变量 `MAVEN_HOME` = `C:\Program Files\Apache\maven`
   - 编辑 `Path`，添加 `%MAVEN_HOME%\bin`
4. 验证安装：打开命令行输入 `mvn --version`

### 2. MySQL（必需）

**下载地址：** https://dev.mysql.com/downloads/mysql/

**安装步骤：**
1. 下载 MySQL 8.0 安装包
2. 运行安装程序，设置 root 密码
3. 安装 Navicat Premium（可选，用于数据库管理）

### 3. Android Studio（必需）

**下载地址：** https://developer.android.com/studio

**安装步骤：**
1. 下载并安装 Android Studio
2. 启动后安装 Android SDK（API 24+）
3. 创建或启动 Android 模拟器

## 🚀 完整运行步骤

### 第一步：启动 MySQL

1. 打开命令提示符（管理员）
2. 启动 MySQL 服务：
```cmd
net start mysql80
```

3. 创建数据库：
```cmd
mysql -u root -p
```
输入密码后执行：
```sql
CREATE DATABASE campus_trade DEFAULT CHARACTER SET utf8mb4;
USE campus_trade;
SOURCE d:\MyApplication5\backend\src\main\resources\schema.sql;
SOURCE d:\MyApplication5\backend\src\main\resources\data.sql;
```

### 第二步：启动后端服务

1. 打开命令提示符
2. 进入后端目录：
```cmd
cd d:\MyApplication5\backend
```

3. 编译项目：
```cmd
mvn clean install
```

4. 启动服务：
```cmd
mvn spring-boot:run
```

5. 等待看到以下输出表示启动成功：
```
Started CampustradeApplication in X.XXX seconds
```

6. 测试后端：打开浏览器访问 http://localhost:8080

### 第三步：运行 Android 应用

1. **启动 Android Studio**
   - 打开项目：File -> Open -> 选择 `d:\MyApplication5`
   - 等待 Gradle 同步完成

2. **启动模拟器**
   - 点击工具栏的 Device Manager
   - 创建或启动一个模拟器（推荐 API 30+）

3. **运行应用**
   - 确保顶部工具栏显示 `app`
   - 点击绿色运行按钮（▶）
   - 或按快捷键 `Shift + F10`

4. **测试登录**
   - 用户名：zhangsan
   - 密码：123456

## 🔧 常见问题

### 问题 1：Maven 命令无法识别

**解决：**
- 检查环境变量是否配置正确
- 重启命令提示符
- 验证：`mvn --version`

### 问题 2：MySQL 连接失败

**解决：**
- 检查 MySQL 服务是否启动：`net start mysql80`
- 检查用户名密码是否正确
- 修改 `backend/src/main/resources/application.yml` 中的配置

### 问题 3：Android Studio Gradle 同步失败

**解决：**
- File -> Invalidate Caches and Restart
- 检查网络连接
- 使用国内镜像源

### 问题 4：Android 应用无法连接后端

**解决：**
- 确保后端服务正在运行
- 模拟器使用 `http://10.0.2.2:8080`（已配置）
- 真机调试需要修改为电脑 IP 地址

## 📱 快速测试

### 后端 API 测试

使用 Postman 或浏览器测试：

```bash
# 获取分类列表
GET http://localhost:8080/api/categories

# 获取商品列表
GET http://localhost:8080/api/items?page=1&size=10

# 用户登录
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456"
}
```

### Android 应用测试

1. 启动应用后进入登录页面
2. 输入测试账号：
   - 用户名：zhangsan
   - 密码：123456
3. 点击登录
4. 查看商品列表
5. 尝试搜索商品
6. 点击分类筛选

## 🎯 验证清单

运行前请确认：

- [ ] Maven 已安装并配置环境变量
- [ ] MySQL 已安装并启动
- [ ] 数据库 `campus_trade` 已创建
- [ ] 后端服务已启动（http://localhost:8080）
- [ ] Android Studio 已安装
- [ ] Android SDK 已下载（API 24+）
- [ ] 模拟器已启动
- [ ] Gradle 同步成功

## 📞 需要帮助？

如果遇到问题，请检查：
1. 控制台错误信息
2. 日志文件
3. 网络连接
4. 端口占用情况

---

**提示：** 首次运行可能需要较长时间下载依赖，请耐心等待。