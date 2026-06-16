# 校园闲置物品交易 APP

## 项目简介

这是一个功能完善的校园闲置物品交易系统，基于 Android + Spring Boot + MySQL 技术栈开发。

**当前版本：v1.12.0** | 构建日期：2026-06-16

## 主要功能

### 用户模块
- 用户注册与登录
- 个人信息管理（姓名、学号、手机号）
- 修改密码
- 用户头像上传

### 商品模块
- 商品列表展示（下拉刷新、自动加载更多）
- 商品分类筛选
- 商品搜索功能
- 商品详情查看
- 商品图片预览（支持多图切换全屏预览）
- 商品发布（含图片上传）
- 商品上下架管理

### 订单模块
- 订单创建与支付
- 订单状态管理（待付款、待发货、待收货、已完成、已取消）
- 订单倒计时显示（待付款订单实时倒计时）
- 订单详情查看
- 修改收货地址和联系电话

### 收藏模块
- 添加/取消收藏
- 收藏列表展示
- 下拉刷新

### 聊天模块
- 买卖双方实时沟通

## 项目结构

```
MyApplication5/
├── app/                              # Android 客户端
│   ├── src/main/
│   │   ├── java/com/example/myapplication/
│   │   │   ├── LoginActivity.java          # 登录页面
│   │   │   ├── RegisterActivity.java       # 注册页面
│   │   │   ├── MainActivity.java          # 主页面
│   │   │   ├── ProfileActivity.java       # 个人中心
│   │   │   ├── MyItemsActivity.java       # 我的商品
│   │   │   ├── PublishActivity.java       # 发布商品
│   │   │   ├── ItemDetailActivity.java    # 商品详情
│   │   │   ├── OrderActivity.java         # 订单列表
│   │   │   ├── OrderDetailActivity.java    # 订单详情
│   │   │   ├── PaymentActivity.java       # 支付页面
│   │   │   ├── FavoriteActivity.java      # 收藏列表
│   │   │   ├── ChatActivity.java          # 聊天页面
│   │   │   ├── ImagePreviewActivity.java  # 图片预览
│   │   │   ├── SettingsActivity.java       # 设置页面
│   │   │   ├── network/                   # 网络请求
│   │   │   ├── model/                     # 数据模型
│   │   │   └── util/                      # 工具类
│   │   └── res/                           # 资源文件
│   └── build.gradle.kts
└── backend/                           # Spring Boot 后端
    ├── src/main/
    │   ├── java/com/example/campustrade/
    │   │   ├── controller/                 # REST API 控制器
    │   │   ├── service/                   # 业务逻辑
    │   │   ├── repository/                # 数据访问
    │   │   ├── entity/                    # 实体类
    │   │   └── dto/                        # 数据传输对象
    │   └── resources/
    │       ├── application.yml            # 配置文件
    │       ├── schema.sql                  # 数据库脚本
    │       ├── data.sql                    # 测试数据
    │       └── images/                     # 商品图片
    └── pom.xml
```

## 技术栈

### Android 客户端
- **语言：** Java
- **最低 SDK：** API 24 (Android 7.0)
- **目标 SDK：** API 34 (Android 14)
- **网络：** OkHttp 4.12
- **JSON：** Gson 2.11
- **图片加载：** Glide 4.16
- **UI：** Material Design 3

### 后端服务
- **框架：** Spring Boot 3.2.0
- **ORM：** Spring Data JPA
- **数据库：** MySQL 8.0
- **构建：** Maven 3.8+

## 运行步骤

### 1. 启动后端服务

**前提条件：**
- JDK 21+
- MySQL 8.0+
- Maven 3.8+

**步骤：**

1. 创建数据库并导入数据：
```bash
# 连接 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE campus_trade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出 MySQL
exit;

# 导入数据库结构
mysql -u root -p campus_trade < backend/src/main/resources/schema.sql

# 导入测试数据
mysql -u root -p campus_trade < backend/src/main/resources/data.sql
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
- Android Studio Hedgehog 或更高版本
- Android SDK (API 24+)
- Android 模拟器或真机

**步骤：**

1. 在 Android Studio 中打开项目：
   - 文件 -> 打开 -> 选择 `MyApplication5` 目录

2. 修改网络请求地址（如果需要）：
   - 打开 `app/src/main/java/com/example/myapplication/network/ApiService.java`
   - 确认 `BASE_URL = "http://10.0.2.2:8080/api/"`
   - 如果使用真机调试，需要改为电脑 IP 地址

3. 同步 Gradle 依赖：
   - 点击 "Sync Now" 或 File -> Sync Project with Gradle Files

4. 运行应用：
   - 选择目标设备（模拟器或真机）
   - 点击运行按钮（绿色三角形）或 Shift+F10
   - **注意：运行整个应用，不要运行单个 Java 文件**

## 测试账号

后端已预置以下测试账号：

| 用户名 | 密码 | 姓名 | 学号 |
|--------|------|------|------|
| zhangsan | 123456 | 张三 | 2021001 |
| lisi | 123456 | 李四 | 2021002 |
| wangwu | 123456 | 王五 | 2021003 |
| zhaoliu | 123456 | 赵六 | 2021004 |
| sunqi | 123456 | 孙七 | 2021005 |

## API 接口

| 模块 | 方法 | 接口 | 说明 |
|------|------|------|------|
| 用户 | POST | /api/users/register | 用户注册 |
| 用户 | POST | /api/users/login | 用户登录 |
| 用户 | GET | /api/users/{id} | 获取用户信息 |
| 用户 | PUT | /api/users/{id} | 更新用户信息 |
| 用户 | PUT | /api/users/{id}/password | 修改密码 |
| 用户 | POST | /api/users/{id}/avatar | 上传头像 |
| 商品 | GET | /api/items | 获取商品列表 |
| 商品 | GET | /api/items/search | 搜索商品 |
| 商品 | GET | /api/items/category/{id} | 分类筛选 |
| 商品 | GET | /api/items/user/{id} | 用户商品列表 |
| 商品 | GET | /api/items/{id} | 商品详情 |
| 商品 | POST | /api/items | 发布商品 |
| 商品 | PUT | /api/items/{id}/status | 更新商品状态 |
| 商品 | POST | /api/items/upload-image | 上传商品图片 |
| 分类 | GET | /api/categories | 获取分类列表 |
| 订单 | POST | /api/orders | 创建订单 |
| 订单 | GET | /api/orders/buyer/{id} | 买家订单列表 |
| 订单 | GET | /api/orders/seller/{id} | 卖家订单列表 |
| 订单 | GET | /api/orders/{id} | 订单详情 |
| 订单 | PUT | /api/orders/{id}/status | 更新订单状态 |
| 订单 | PUT | /api/orders/{id}/pay | 支付订单 |
| 收藏 | GET | /api/favorites/{userId} | 获取收藏列表 |
| 收藏 | POST | /api/favorites | 添加收藏 |
| 收藏 | DELETE | /api/favorites/{userId}/{itemId} | 取消收藏 |
| 聊天 | GET | /api/chats/{userId1}/{userId2} | 获取聊天记录 |
| 聊天 | POST | /api/chats | 发送消息 |

## 常见问题

### 1. 网络请求失败

**问题：** Android 应用无法连接后端服务

**解决方案：**
- 确保后端服务已启动并运行在 http://localhost:8080
- 模拟器使用 http://10.0.2.2:8080
- 真机调试需要：
  - 手机和电脑在同一局域网
  - 修改为电脑 IP 地址，如 http://192.168.1.100:8080
  - 关闭防火墙或添加端口例外

### 2. 数据库连接失败

**问题：** 后端启动时报数据库连接错误

**解决方案：**
- 检查 MySQL 服务是否启动
- 确认数据库用户名密码正确
- 确认数据库 campus_trade 已创建
- 检查 application.yml 中的数据库配置

### 3. Gradle 同步失败

**问题：** Android Studio 无法同步 Gradle

**解决方案：**
- 检查网络连接
- File -> Invalidate Caches and Restart
- 点击 Sync Project with Gradle Files

### 4. 图片无法显示

**问题：** 商品图片无法加载

**解决方案：**
- 确保后端已启动
- 检查 images 文件夹是否存在
- 确认图片路径是否正确

## 版本更新

### v1.12.0 (2026-06-16)
- 更新 data.sql 中所有商品图片地址为本地 images 文件夹路径

### v1.11.0 (2026-06-16)
- 性能优化和界面改进

### v1.10.0 (2026-06-15)
- 我的商品页面添加上下架功能
- 全局线程池管理优化
- UI按钮布局优化

### v1.9.0 (2026-06-14)
- 订单倒计时功能
- 支持修改收货地址和联系电话

### v1.8.0 (2026-06-14)
- 商品图片预览功能

### v1.7.0 (2026-06-14)
- 发布商品图片上传

### v1.6.0 (2026-06-14)
- 下拉刷新功能

### v1.5.0 (2026-06-14)
- 用户头像功能
- 订单状态筛选
- 订单详情页面

## 开发工具

- Android Studio Hedgehog
- IntelliJ IDEA
- Navicat Premium（数据库管理）
- Postman（API 测试）

## 许可证

本项目仅供学习和教育使用。
