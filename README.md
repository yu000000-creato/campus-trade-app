# 校园闲置物品交易 APP

## 项目说明

这是一个基于 Android + Spring Boot + MySQL 的校园闲置物品交易系统。

## 项目结构

```
MyApplication5/
├── app/                          # Android 客户端
│   ── src/main/
│       ├── java/com/example/myapplication/
│       │   ├── LoginActivity.java      # 登录页面
│       │   ├── RegisterActivity.java   # 注册页面
│       │   ├── MainActivity.java       # 主页面
│       │   ├── network/                # 网络请求
│       │   └── model/                  # 数据模型
│       ├── res/                        # 资源文件
│       └── AndroidManifest.xml         # 配置文件
└── backend/                      # Spring Boot 后端
    └── src/main/
        ├── java/com/example/campustrade/
        │   ├── controller/         # REST API 控制器
        │   ├── service/            # 业务逻辑
        │   ├── repository/         # 数据访问
        │   ├── entity/             # 实体类
        │   └── dto/                # 数据传输对象
        └── resources/
            ├── application.yml     # 配置文件
            └── schema.sql          # 数据库脚本
```

## 运行步骤

### 1. 启动后端服务

**前提条件：**
- 安装 JDK 21+
- 安装 MySQL 8.0+
- 安装 Maven 3.8+

**步骤：**

1. 创建数据库并导入数据：
```bash
# 使用 Navicat 或命令行连接 MySQL
mysql -u root -p

# 执行后端目录下的 SQL 脚本
source backend/src/main/resources/schema.sql
source backend/src/main/resources/data.sql
```

2. 修改配置文件 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trade?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username  # 修改为你的数据库用户名
    password: your_password  # 修改为你的数据库密码
```

3. 启动 Spring Boot 应用：
```bash
cd backend
mvn spring-boot:run
```

服务启动后访问：http://localhost:8080

### 2. 运行 Android 应用

**前提条件：**
- 安装 Android Studio
- 配置 Android SDK (API 24+)
- 启动 Android 模拟器或连接真机

**步骤：**

1. 在 Android Studio 中打开项目：
   - 文件 -> 打开 -> 选择 `MyApplication5` 目录

2. 修改网络请求地址（如果需要）：
   - 打开 `app/src/main/java/com/example/myapplication/network/ApiService.java`
   - 确认 `BASE_URL = "http://10.0.2.2:8080/api/"`
   - 如果使用真机调试，需要改为你的电脑 IP 地址

3. 同步 Gradle 依赖：
   - 点击 "Sync Now" 或 File -> Sync Project with Gradle Files

4. 运行应用：
   - 选择目标设备（模拟器或真机）
   - 点击运行按钮（绿色三角形）或 Shift+F10
   - **注意：运行整个应用，不要运行单个 Java 文件**

## 测试账号

后端已预置以下测试账号：

| 用户名 | 密码 | 学号 |
|--------|------|------|
| zhangsan | 123456 | 2021001 |
| lisi | 123456 | 2021002 |
| wangwu | 123456 | 2021003 |

## 功能说明

### Android 客户端

1. **登录/注册**
   - 用户注册功能
   - 用户名密码登录
   - 登录状态保持

2. **首页**
   - 商品分类展示
   - 商品列表浏览
   - 商品搜索功能
   - 分类筛选功能

3. **底部导航**
   - 首页
   - 收藏（开发中）
   - 发布（开发中）
   - 订单（开发中）
   - 我的（开发中）

### 后端 API

| 模块 | 接口 | 说明 |
|------|------|------|
| 用户 | POST /api/users/register | 用户注册 |
| 用户 | POST /api/users/login | 用户登录 |
| 商品 | GET /api/items | 获取商品列表 |
| 商品 | GET /api/items/search | 搜索商品 |
| 商品 | GET /api/items/category/{id} | 分类筛选 |
| 分类 | GET /api/categories | 获取分类列表 |
| 订单 | POST /api/orders | 创建订单 |
| 订单 | GET /api/orders/buyer/{id} | 买家订单 |
| 订单 | GET /api/orders/seller/{id} | 卖家订单 |

## 常见问题

### 1. 网络请求失败

**问题：** Android 应用无法连接后端服务

**解决方案：**
- 确保后端服务已启动
- 模拟器使用 `http://10.0.2.2:8080`
- 真机调试需要：
  - 手机和电脑在同一局域网
  - 修改为电脑 IP 地址，如 `http://192.168.1.100:8080`
  - 关闭防火墙或添加端口例外

### 2. 数据库连接失败

**问题：** 后端启动时报数据库连接错误

**解决方案：**
- 检查 MySQL 服务是否启动
- 确认数据库用户名密码正确
- 确认数据库 `campus_trade` 已创建

### 3. Gradle 同步失败

**问题：** Android Studio 无法同步 Gradle

**解决方案：**
- 检查网络连接
- 使用国内镜像源
- File -> Invalidate Caches and Restart

## 技术栈

**Android 客户端：**
- 语言：Java
- 网络：OkHttp 4.12
- JSON：Gson 2.11
- UI：Material Design

**后端服务：**
- 框架：Spring Boot 3.2.0
- ORM：Spring Data JPA
- 数据库：MySQL 8.0
- 构建：Maven

## 开发工具

- Android Studio
- IntelliJ IDEA / Eclipse
- Navicat Premium（数据库管理）
- Postman（API 测试）