# 版本变更日志

## v1.1.0 (2026-06-11)

### 新增功能

1. **我的商品功能**
   - 新增 MyItemsActivity 页面
   - 展示当前用户发布的商品列表
   - 支持点击商品进入详情页

2. **设置功能**
   - 新增 SettingsActivity 页面
   - 支持编辑个人资料（姓名、学号、手机号）
   - 支持修改密码
   - 显示关于我们信息

3. **联系卖家功能**
   - 修复 ChatActivity 布局文件问题
   - 添加完整的异常处理和日志输出
   - 支持从商品详情页跳转到聊天页面

### 修复问题

1. **首页商品无法点击问题**
   - 修复 Gson 无法解析 LocalDateTime 类型的问题
   - 在 ApiService 中配置自定义类型适配器

2. **Activity 崩溃问题**
   - 移除所有 Activity 中的 `getSupportActionBar().setTitle()` 调用
   - 修复主题配置与代码的兼容性问题

3. **收藏功能问题**
   - 修复取消收藏后商品仍在列表中的问题
   - 在 FavoriteActivity 中添加 onResume() 方法刷新列表

4. **订单页面问题**
   - 修复订单页面不显示数据问题
   - 创建 OrderResponse 模型统一解析

5. **RecyclerView 布局问题**
   - 修复 activity_chat.xml 中 RecyclerView 包名路径问题

### 技术改进

1. 统一使用 ApiService 中的 Gson 实例进行 JSON 解析
2. 修复后端 DTO 中时间格式转换问题
3. 在 AndroidManifest.xml 中注册所有 Activity
4. 增加数据库测试数据（用户、商品、订单、收藏、聊天记录）

### 文件变更

**新增文件:**
- app/src/main/java/com/example/myapplication/MyItemsActivity.java
- app/src/main/java/com/example/myapplication/SettingsActivity.java
- app/src/main/java/com/example/myapplication/model/OrderResponse.java
- app/src/main/res/layout/activity_my_items.xml
- app/src/main/res/layout/activity_settings.xml
- app/src/main/res/layout/dialog_change_password.xml
- app/src/main/res/layout/dialog_edit_profile.xml
- app/src/main/res/layout/item_goods.xml
- CHANGELOG.md

**修改文件:**
- app/src/main/AndroidManifest.xml - 注册 Activity
- app/src/main/java/com/example/myapplication/ChatActivity.java - 修复崩溃问题
- app/src/main/java/com/example/myapplication/ItemDetailActivity.java - 联系卖家逻辑
- app/src/main/java/com/example/myapplication/OrderActivity.java - 修复解析问题
- app/src/main/java/com/example/myapplication/ProfileActivity.java - 添加页面跳转
- app/src/main/java/com/example/myapplication/network/ApiService.java - Gson 配置
- app/src/main/res/layout/activity_chat.xml - 修复 RecyclerView 路径
- backend/src/main/java/com/example/campustrade/dto/response/*.java - 时间格式修复
- backend/src/main/java/com/example/campustrade/service/impl/*.java - 时间格式转换

## v1.0.0 (初始版本)

### 基础功能

1. 用户注册与登录
2. 商品列表展示
3. 商品详情查看
4. 商品分类筛选
5. 商品搜索功能
6. 收藏功能
7. 订单功能