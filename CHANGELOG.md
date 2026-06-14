# 版本变更日志

## v1.9.0 (2026-06-14)

### 新增功能

1. **订单倒计时功能**
   - 待付款订单显示实时倒计时
   - 支持时:分:秒格式显示
   - 超时自动提示并刷新订单状态

2. **订单信息编辑**
   - 支持修改收货地址
   - 支持修改联系电话
   - 仅限买家在待付款状态编辑

### Bug修复

- 修复时区问题导致倒计时显示错误
- 统一前后端使用UTC时间计算

### 文件变更

**后端:**
- backend/src/main/java/com/example/campustrade/entity/Order.java
- backend/src/main/java/com/example/campustrade/dto/response/OrderResponse.java
- backend/src/main/java/com/example/campustrade/dto/request/OrderCreateRequest.java
- backend/src/main/java/com/example/campustrade/service/OrderService.java
- backend/src/main/java/com/example/campustrade/service/impl/OrderServiceImpl.java
- backend/src/main/java/com/example/campustrade/controller/OrderController.java

**前端:**
- app/src/main/java/com/example/myapplication/model/OrderResponse.java
- app/src/main/java/com/example/myapplication/OrderDetailActivity.java
- app/src/main/res/layout/activity_order_detail.xml

## v1.8.0 (2026-06-14)

### 新增功能

1. **商品图片预览功能**
   - 商品详情页顶部显示商品图片
   - 点击商品图片进入全屏预览
   - 商品列表中点击图片可预览
   - 支持多张图片切换查看
   - 显示当前图片序号

### 界面改进

- 商品详情页布局优化，增加图片展示区域
- 图片预览页全屏黑色背景，更好的视觉效果

### 文件变更

**新增文件:**
- app/src/main/java/com/example/myapplication/ImagePreviewActivity.java
- app/src/main/res/layout/activity_image_preview.xml

**修改文件:**
- app/src/main/java/com/example/myapplication/ItemDetailActivity.java
- app/src/main/java/com/example/myapplication/MainActivity.java
- app/src/main/res/layout/activity_item_detail.xml
- app/src/main/AndroidManifest.xml

## v1.7.0 (2026-06-14)

### 新增功能

1. **发布商品图片上传**
   - 发布商品时支持选择相册图片
   - 图片自动上传到服务器
   - 发布页面显示图片预览
   - 商品信息包含图片URL

### 后端改进

- 新增 `POST /api/items/upload-image` 图片上传接口
- 支持图片文件存储和访问

### 文件变更

**新增/修改文件:**
- backend/src/main/java/com/example/campustrade/controller/ItemController.java - 添加图片上传接口
- backend/src/main/java/com/example/campustrade/service/ItemService.java - 添加uploadImage方法
- backend/src/main/java/com/example/campustrade/service/impl/ItemServiceImpl.java - 实现图片上传逻辑
- app/src/main/java/com/example/myapplication/network/ApiService.java - 添加uploadImage方法
- app/src/main/java/com/example/myapplication/PublishActivity.java - 添加图片选择和上传逻辑
- app/src/main/res/layout/activity_publish.xml - 添加图片选择区域
- app/src/main/AndroidManifest.xml - 添加存储权限

## v1.6.0 (2026-06-14)

### 新增功能

1. **下拉刷新功能**
   - 首页商品列表支持下拉刷新
   - 订单页面支持下拉刷新
   - 收藏页面支持下拉刷新
   - 我的发布页面支持下拉刷新
   - 刷新动画颜色与页面主题一致

### 优化改进

- 优化首页布局结构，刷新图标显示在商品列表上方
- 修复登录后闪退的问题（setColorSchemeResources 参数错误）

### 文件变更

**修改文件:**
- app/build.gradle.kts - 添加 SwipeRefreshLayout 依赖
- app/src/main/res/layout/activity_main.xml - 添加下拉刷新布局
- app/src/main/res/layout/activity_order.xml - 添加下拉刷新布局
- app/src/main/res/layout/activity_favorite.xml - 添加下拉刷新布局
- app/src/main/res/layout/activity_my_items.xml - 添加下拉刷新布局
- app/src/main/java/com/example/myapplication/MainActivity.java - 添加下拉刷新逻辑
- app/src/main/java/com/example/myapplication/OrderActivity.java - 添加下拉刷新逻辑
- app/src/main/java/com/example/myapplication/FavoriteActivity.java - 添加下拉刷新逻辑
- app/src/main/java/com/example/myapplication/MyItemsActivity.java - 添加下拉刷新逻辑

## v1.5.0 (2026-06-14)

### 新增功能

1. **用户头像功能**
   - 支持用户上传头像
   - 个人中心展示用户头像
   - 点击头像可选择相册图片上传
   - 后端支持头像文件存储和访问

2. **订单状态筛选**
   - 订单页面添加状态筛选 Tab
   - 支持筛选：全部/待付款/待发货/待收货/已完成/已取消
   - 不同状态显示不同颜色标识

3. **订单详情页面**
   - 新增 OrderDetailActivity 订单详情页
   - 显示订单状态、商品信息、订单信息、交易双方信息
   - 根据订单状态显示不同操作按钮
   - 支持付款、取消订单、确认收货、联系卖家等操作

### 文件变更

**新增文件:**
- app/src/main/java/com/example/myapplication/OrderDetailActivity.java
- app/src/main/res/layout/activity_order_detail.xml

**修改文件:**
- app/src/main/java/com/example/myapplication/ProfileActivity.java
- app/src/main/java/com/example/myapplication/OrderActivity.java
- app/src/main/java/com/example/myapplication/network/ApiService.java
- app/src/main/res/layout/activity_profile.xml
- app/src/main/res/layout/activity_order.xml
- app/src/main/AndroidManifest.xml
- backend/src/main/java/com/example/campustrade/controller/UserController.java
- backend/src/main/java/com/example/campustrade/controller/OrderController.java
- backend/src/main/java/com/example/campustrade/service/UserService.java
- backend/src/main/java/com/example/campustrade/service/OrderService.java
- backend/src/main/java/com/example/campustrade/service/impl/UserServiceImpl.java
- backend/src/main/java/com/example/campustrade/service/impl/OrderServiceImpl.java
- backend/src/main/java/com/example/campustrade/config/WebConfig.java

## v1.4.0 (2026-06-12)

### UI优化

1. **点击效果优化**
   - 为所有按钮添加 Ripple 点击效果
   - 为列表项添加点击反馈效果
   - 新增多种主题的 Ripple 效果文件：
     - `ripple_primary.xml` - 紫色主题 (#6b46c1)
     - `ripple_indigo.xml` - 靛蓝主题 (#667eea)
     - `ripple_white.xml` - 白色背景点击效果
     - `ripple_transparent.xml` - 透明背景点击效果
     - `ripple_payment.xml` - 支付按钮 (#6c5ce7)

2. **修复问题**
   - 修复登录页面"立即注册"文字看不见的问题
   - 修复注册页面"立即登录"文字看不见的问题

### 文件变更

**新增文件:**
- app/src/main/res/drawable/ripple_primary.xml
- app/src/main/res/drawable/ripple_indigo.xml
- app/src/main/res/drawable/ripple_white.xml
- app/src/main/res/drawable/ripple_transparent.xml
- app/src/main/res/drawable/ripple_payment.xml

**修改文件:**
- app/src/main/res/layout/activity_login.xml
- app/src/main/res/layout/activity_register.xml
- app/src/main/res/layout/activity_main.xml
- app/src/main/res/layout/activity_publish.xml
- app/src/main/res/layout/activity_item_detail.xml
- app/src/main/res/layout/activity_chat.xml
- app/src/main/res/layout/activity_payment.xml
- app/src/main/res/layout/activity_settings.xml
- app/src/main/res/layout/activity_profile.xml
- app/src/main/res/layout/item_home_goods.xml
- app/src/main/res/layout/item_goods.xml

## v1.3.0 (2026-06-12)

### 性能优化

1. **商品列表性能优化**
   - 将商品列表从 LinearLayout 改为 RecyclerView，实现视图复用
   - 启用 Glide 图片缓存，解决图片加载卡顿问题
   - 移除冗余的 HttpURLConnection 备用加载逻辑

2. **布局优化**
   - 修复 RecyclerView 嵌套在 ScrollView 中导致商品显示不全的问题
   - 优化布局结构，提升滚动流畅度

### 新增功能

1. **自动加载下一页（无限滚动）**
   - 滚动到底部自动加载更多商品
   - 无需手动点击"查看更多"按钮

2. **底部状态提示**
   - 加载中显示进度条和"加载中..."文字
   - 没有更多数据时显示"已到最后"

### 修复问题

1. **商品展示不全**
   - 修复 RecyclerView 高度计算问题
   - 确保所有商品都能正确显示

### 文件变更

**新增文件:**
- app/src/main/res/layout/item_home_goods.xml - 商品列表项布局
- app/src/main/res/layout/item_load_more.xml - 底部加载状态布局

**修改文件:**
- app/src/main/java/com/example/myapplication/MainActivity.java - 核心优化
- app/src/main/res/layout/activity_main.xml - 布局结构调整

## v1.2.0 (2026-06-12)

### 新增功能

1. **排序功能优化**
   - 修复按浏览次数排序不生效问题
   - 确保前端正确传递排序参数到后端
   - 优化商品列表展示逻辑

### 修复问题

1. **排序功能问题**
   - 修复按浏览次数排序时商品顺序不正确的问题
   - 确保排序参数正确传递和处理

2. **商品列表展示问题**
   - 优化商品列表加载逻辑
   - 确保数据正确显示

### 文件变更

**修改文件:**
- app/src/main/java/com/example/myapplication/MainActivity.java - 排序逻辑优化
- app/src/main/java/com/example/myapplication/MyItemsActivity.java - 商品列表优化
- app/src/main/java/com/example/myapplication/OrderActivity.java - 订单展示优化
- app/src/main/java/com/example/myapplication/model/OrderResponse.java - 订单响应模型
- app/src/main/res/layout/activity_main.xml - 布局优化
- app/src/main/res/layout/item_goods.xml - 商品项布局优化
- backend/src/main/java/com/example/campustrade/controller/ItemController.java - 排序参数处理
- backend/src/main/java/com/example/campustrade/dto/response/OrderResponse.java - 订单响应DTO
- backend/src/main/java/com/example/campustrade/service/ItemService.java - 服务接口
- backend/src/main/java/com/example/campustrade/service/impl/ItemServiceImpl.java - 排序逻辑实现
- backend/src/main/java/com/example/campustrade/service/impl/OrderServiceImpl.java - 订单服务优化

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
