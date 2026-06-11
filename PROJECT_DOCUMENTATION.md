# 校园闲置物品交易 APP - 完整项目文档

## 项目概述

这是一个完整的校园闲置物品交易系统，采用前后端分离架构：
- **前端**：Android 原生应用（Java）
- **后端**：Spring Boot REST API（Java）
- **数据库**：MySQL 8.0
- **管理工具**：Navicat Premium

## 技术架构

```
┌─────────────────┐         ┌─────────────────┐
│   Android App   │◄───────►│  Spring Boot    │
│   (Java)        │  HTTP   │  REST API       │
│                 │  JSON   │                 │
└─────────────────┘         └────────┬────────┘
                                     │
                              ┌──────▼────────┐
                              │   MySQL 8.0   │
                              │   Database    │
                              └───────────────┘
```

## 项目结构

```
MyApplication5/
│
├── app/                                    # Android 客户端模块
│   ├── src/main/
│   │   ├── java/com/example/myapplication/
│   │   │   ├── LoginActivity.java          # 登录 Activity
│   │   │   ├── RegisterActivity.java       # 注册 Activity
│   │   │   ├── MainActivity.java           # 主页 Activity
│   │   │   ├── network/
│   │   │   │   └── ApiService.java         # 网络请求服务
│   │   │   ── model/
│   │   │       ├── User.java               # 用户数据模型
│   │   │       ├── Item.java               # 商品数据模型
│   │   │       ├── Category.java           # 分类数据模型
│   │   │       ├── Result.java             # 响应结果封装
│   │   │       └── PageResult.java         # 分页结果封装
│   │   │
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_login.xml      # 登录页面布局
│   │   │   │   ├── activity_register.xml   # 注册页面布局
│   │   │   │   └── activity_main.xml       # 主页面布局
│   │   │   ├── drawable/
│   │   │   │   ├── btn_primary.xml         # 按钮样式
│   │   │   │   ├── login_card.xml          # 卡片样式
│   │   │   │   ├── search_input.xml        # 搜索框样式
│   │   │   │   ├── category_bg.xml         # 分类背景
│   │   │   │   └── item_bg.xml             # 商品卡片背景
│   │   │   └── values/
│   │   │       └── colors.xml              # 颜色定义
│   │   │
│   │   ── AndroidManifest.xml             # 应用清单文件
│   │
│   ── build.gradle.kts                    # Gradle 构建配置
│
├── backend/                                # Spring Boot 后端模块
│   ├── src/main/
│   │   ├── java/com/example/campustrade/
│   │   │   ├── CampustradeApplication.java # 启动类
│   │   │   │
│   │   │   ├── controller/                 # 控制层（6 个）
│   │   │   │   ├── UserController.java     # 用户接口
│   │   │   │   ├── ItemController.java     # 商品接口
│   │   │   │   ├── OrderController.java    # 订单接口
│   │   │   │   ├── CategoryController.java # 分类接口
│   │   │   │   ├── FavoriteController.java # 收藏接口
│   │   │   │   └── ChatController.java     # 聊天接口
│   │   │   │
│   │   │   ├── service/                    # 服务层
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ItemService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── FavoriteService.java
│   │   │   │   └── ChatService.java
│   │   │   │
│   │   │   ├── service/impl/               # 服务实现
│   │   │   │   └── UserServiceImpl.java 等
│   │   │   │
│   │   │   ├── repository/                 # 数据访问层（6 个）
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ItemRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── FavoriteRepository.java
│   │   │   │   └── ChatRepository.java
│   │   │   │
│   │   │   ├── entity/                     # 实体类（6 个）
│   │   │   │   ├── User.java               # 用户实体
│   │   │   │   ├── Item.java               # 商品实体
│   │   │   │   ├── Order.java              # 订单实体
│   │   │   │   ├── Category.java           # 分类实体
│   │   │   │   ├── Favorite.java           # 收藏实体
│   │   │   │   └── Chat.java               # 聊天实体
│   │   │   │
│   │   │   ├── dto/                        # 数据传输对象
│   │   │   │   ├── Result.java             # 统一响应
│   │   │   │   ├── PageResult.java         # 分页响应
│   │   │   │   └── request/                # 请求 DTO
│   │   │   │       ├── UserRegisterRequest.java
│   │   │   │       ├── UserLoginRequest.java
│   │   │   │       ├── ItemCreateRequest.java
│   │   │   │       ├── OrderCreateRequest.java
│   │   │   │       └── ChatSendRequest.java
│   │   │   │
│   │   │   └── response/                   # 响应 DTO
│   │   │       ├── UserResponse.java
│   │   │       ├── ItemResponse.java
│   │   │       ├── OrderResponse.java
│   │   │       └── ChatResponse.java
│   │   │
│   │   ├── config/                         # 配置类
│   │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   │   └── WebConfig.java              # Web 配置（CORS）
│   │   │
│   │   └── resources/
│   │       ├── application.yml             # 应用配置文件
│   │       ├── schema.sql                  # 数据库建表脚本
│   │       └── data.sql                    # 初始化数据脚本
│   │
│   └── pom.xml                             # Maven 配置文件
│
└── README.md                               # 项目说明文档
```

## 数据库设计

### 1. 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(100) | 密码 |
| real_name | VARCHAR(50) | 真实姓名 |
| student_id | VARCHAR(20) | 学号（唯一） |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| avatar | VARCHAR(255) | 头像 |
| status | INT | 状态（0 禁用/1 启用） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 2. 商品表 (items)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 发布者 ID |
| category_id | BIGINT | 分类 ID |
| title | VARCHAR(100) | 标题 |
| description | TEXT | 描述 |
| original_price | DECIMAL(10,2) | 原价 |
| current_price | DECIMAL(10,2) | 现价 |
| images | TEXT | 图片（JSON 数组） |
| status | INT | 状态（0 下架/1 在售/2 已卖） |
| view_count | INT | 浏览次数 |

### 3. 订单表 (orders)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR(50) | 订单编号 |
| item_id | BIGINT | 商品 ID |
| buyer_id | BIGINT | 买家 ID |
| seller_id | BIGINT | 卖家 ID |
| price | DECIMAL(10,2) | 成交价 |
| status | INT | 状态（1-5） |
| address | VARCHAR(500) | 交易地点 |

### 4. 分类表 (categories)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 分类名 |
| icon | VARCHAR(255) | 图标 |
| sort_order | INT | 排序 |

### 5. 收藏表 (favorites)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户 ID |
| item_id | BIGINT | 商品 ID |

### 6. 聊天表 (chats)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| sender_id | BIGINT | 发送者 ID |
| receiver_id | BIGINT | 接收者 ID |
| content | TEXT | 消息内容 |
| status | INT | 状态（0 未读/1 已读） |

## API 接口文档

### 用户模块

```
POST /api/users/register      # 用户注册
POST /api/users/login         # 用户登录
GET  /api/users/{id}          # 获取用户信息
PUT  /api/users/{id}          # 更新用户信息
DELETE /api/users/{id}        # 删除用户
```

### 商品模块

```
POST /api/items                    # 发布商品
GET  /api/items                    # 获取商品列表
GET  /api/items/{id}               # 获取商品详情
GET  /api/items/search?keyword=xx  # 搜索商品
GET  /api/items/category/{id}      # 分类筛选
GET  /api/items/user/{id}          # 用户发布的商品
PUT  /api/items/{id}               # 更新商品
DELETE /api/items/{id}             # 删除商品
PUT  /api/items/{id}/status        # 更新商品状态
```

### 订单模块

```
POST /api/orders                   # 创建订单
GET  /api/orders/{id}              # 获取订单详情
GET  /api/orders/orderNo/{no}      # 订单号查询
GET  /api/orders/buyer/{id}        # 买家订单
GET  /api/orders/seller/{id}       # 卖家订单
PUT  /api/orders/{id}/status       # 更新订单状态
```

### 分类模块

```
GET  /api/categories               # 获取分类列表
GET  /api/categories/{id}          # 获取分类详情
POST /api/categories               # 创建分类
PUT  /api/categories/{id}          # 更新分类
DELETE /api/categories/{id}        # 删除分类
```

### 收藏模块

```
POST /api/favorites                # 添加收藏
DELETE /api/favorites              # 取消收藏
GET  /api/favorites/user/{id}      # 收藏列表
GET  /api/favorites/check          # 检查是否收藏
```

### 聊天模块

```
POST /api/chats                    # 发送消息
GET  /api/chats                    # 聊天记录
PUT  /api/chats/read/{id}          # 标记已读
```

## 快速开始

### 1. 环境准备

**必需软件：**
- JDK 11+（后端）
- JDK 8+（Android）
- Android Studio
- MySQL 8.0+
- Maven 3.8+
- Navicat Premium（可选）

### 2. 数据库配置

```sql
-- 1. 创建数据库
CREATE DATABASE campus_trade DEFAULT CHARACTER SET utf8mb4;

-- 2. 执行建表脚本
source backend/src/main/resources/schema.sql

-- 3. 执行初始化数据
source backend/src/main/resources/data.sql
```

### 3. 后端配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trade?useUnicode=true&characterEncoding=utf8
    username: root      # 修改为你的用户名
    password: root      # 修改为你的密码
```

### 4. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

访问 http://localhost:8080 测试

### 5. 运行 Android

1. 在 Android Studio 中打开项目
2. 同步 Gradle 依赖
3. 启动模拟器或连接真机
4. 运行 app 模块（不是单个 Java 文件！）

## 测试数据

### 测试账号

| 用户名 | 密码 | 学号 | 手机号 |
|--------|------|------|--------|
| zhangsan | 123456 | 2021001 | 13800138001 |
| lisi | 123456 | 2021002 | 13800138002 |
| wangwu | 123456 | 2021003 | 13800138003 |

### 商品数据

系统预置了 5 条商品数据：
- iPhone 14 Pro 256G - ¥5999
- 小米台灯 Pro - ¥150
- 高等数学教材全套 - ¥50
- 羽毛球拍套装 - ¥200
- 耐克运动鞋 - ¥450

### 分类数据

系统预置了 6 个分类：
1. 数码产品
2. 学习资料
3. 生活用品
4. 体育用品
5. 服装鞋帽
6. 其他物品

## 功能特性

### Android 客户端

✅ **已实现：**
- 用户注册/登录
- 商品列表展示
- 商品搜索
- 分类筛选
- 登录状态保持
- 退出登录

 **开发中：**
- 商品详情
- 发布商品
- 收藏功能
- 订单管理
- 聊天功能
- 个人中心

### 后端服务

✅ **已实现：**
- 完整的 REST API
- 数据持久化
- 分页查询
- 条件搜索
- 全局异常处理
- CORS 跨域支持

## 关键技术点

### 1. 网络通信

**Android 端：**
- 使用 OkHttp 进行 HTTP 请求
- Gson 解析 JSON 数据
- 异步线程处理网络请求

**后端：**
- Spring MVC 处理 REST 请求
- @RestController 注解
- 统一响应格式封装

### 2. 数据持久化

- Spring Data JPA
- 实体关系映射（@OneToMany 等）
- 自动建表（ddl-auto: update）

### 3. UI 设计

- Material Design 风格
- LinearLayout 布局
- 自定义 Drawable 背景
- 渐变色按钮

### 4. 安全考虑

- 密码加密（待实现）
- 会话管理（SharedPreferences）
- 输入验证
- SQL 注入防护（JPA）

## 扩展建议

### 功能扩展

1. **图片上传**：添加商品图片上传功能
2. **即时通讯**：WebSocket 实现实时聊天
3. **支付集成**：接入支付宝/微信支付
4. **消息推送**：Firebase 或极光推送
5. **评价系统**：交易完成后互相评价

### 技术优化

1. **缓存机制**：添加 Redis 缓存
2. **图片加载**：使用 Glide 优化图片
3. **下拉刷新**：SwipeRefreshLayout
4. **分页加载**：RecyclerView + Pager
5. **数据加密**：敏感数据加密存储

## 论文写作要点

### 1. 系统架构

- 前后端分离设计
- RESTful API 设计
- MVC 三层架构
- 数据库设计规范

### 2. 技术选型

- 为什么选择 Android 原生开发
- Spring Boot 的优势
- MySQL 数据库特点
- JPA ORM 框架

### 3. 核心功能

- 用户认证流程
- 商品交易流程
- 数据交互协议
- 异常处理机制

### 4. 代码规范

- 命名规范
- 注释规范
- 目录结构
- 版本控制

## 常见问题

### Q1: 如何修改 API 地址？

A: 编辑 `ApiService.java`，修改 `BASE_URL` 常量

### Q2: 数据库连接失败？

A: 检查 MySQL 服务、用户名密码、数据库是否存在

### Q3: Android 无法连接后端？

A: 模拟器使用 10.0.2.2，真机使用电脑 IP 地址

### Q4: Gradle 同步慢？

A: 使用国内镜像源，如阿里云镜像

### Q5: 如何添加新功能？

A: 
1. 数据库添加字段
2. Entity 添加属性
3. Repository 添加方法
4. Service 添加逻辑
5. Controller 添加接口
6. Android 添加调用

## 项目总结

本项目完整实现了校园闲置物品交易系统，涵盖：

- ✅ 完整的 Android 客户端
- ✅ 完整的 Spring Boot 后端
- ✅ 规范的数据库设计
- ✅ RESTful API 接口
- ✅ 前后端分离架构
- ✅ 代码规范注释

适合用于课程设计、毕业设计、实训项目等。

---

**开发时间：** 2024 年
**开发语言：** Java
**适用场景：** 校园二手交易、课程设计、毕业设计