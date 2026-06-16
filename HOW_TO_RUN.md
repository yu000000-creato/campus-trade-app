# 如何运行 Android 应用

## 项目版本
- 当前版本：v1.12.0
- 构建日期：2026-06-16

## 重要提示

**不要直接运行单个 Java 文件！**

在 Android 项目中，应该运行整个应用程序，而不是单个 Java 文件。直接运行 Java 文件会出现 "Main method not found" 错误。

## 正确的运行方式

### 方法一：使用运行按钮（推荐）

1. **确保选择了正确的运行配置**
   - 在 Android Studio 顶部工具栏，应该看到 `app` 而不是某个 Java 文件
   - 如果看到的是 Java 文件名，点击它并选择 `app`

2. **选择目标设备**
   - 确保已启动 Android 模拟器或连接了真机
   - 点击设备选择器，选择目标设备

3. **运行应用**
   - 点击绿色三角形运行按钮（▶）
   - 或按快捷键 `Shift + F10`
   - 或右键点击 `app` 模块 -> Run 'app'

### 方法二：创建运行配置

1. **打开运行配置**
   - 点击顶部工具栏的运行配置下拉框
   - 选择 `Edit Configurations...`

2. **添加 Android App 配置**
   - 点击左上角 `+` 号
   - 选择 `Android App`
   - Module 选择 `MyApplication5.app.main`
   - 点击 `OK`

3. **运行**
   - 选择刚创建的配置
   - 点击运行按钮

### 方法三：右键运行

1. **在项目视图中**
   - 展开 `app` 模块
   - 右键点击 `app` 文件夹

2. **选择运行**
   - 选择 `Run 'app'` 或 `Debug 'app'`

## 运行前检查

### 1. 同步 Gradle

确保 Gradle 已同步：
- 点击 `File` -> `Sync Project with Gradle Files`
- 或点击工具栏的大象图标

### 2. 检查网络权限

确保 `AndroidManifest.xml` 包含网络权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. 启动后端服务

Android 应用需要连接后端 API，确保：
- Spring Boot 后端已启动
- 服务运行在 `http://localhost:8080`
- 模拟器会自动映射为 `http://10.0.2.2:8080`

## 常见问题解决

### 问题 1：提示 "Main method not found"

**原因：** 尝试直接运行 Java 文件而不是 Android 应用

**解决：**
- 不要双击 Java 文件后点击运行
- 确保运行配置选择的是 `app` 而不是某个类
- 按照上述方法重新运行

### 问题 2：Gradle 构建失败

**解决：**
```
1. File -> Invalidate Caches and Restart
2. Build -> Clean Project
3. Build -> Rebuild Project
4. File -> Sync Project with Gradle Files
```

### 问题 3：找不到设备

**解决：**
- 启动 Android 模拟器（Device Manager）
- 或使用真机调试：
  - 开启开发者选项
  - 开启 USB 调试
  - 连接 USB 线

### 问题 4：网络请求失败

**解决：**
- 检查后端服务是否启动
- 模拟器使用 `http://10.0.2.2:8080`
- 真机调试需要：
  - 手机和电脑在同一 WiFi
  - 修改 ApiService.java 中的 BASE_URL 为电脑 IP
  - 关闭防火墙或添加端口例外

## 测试账号

登录时使用以下测试账号：

- 用户名：zhangsan
- 密码：123456

或

- 用户名：lisi
- 密码：123456

## 运行流程

```
1. 启动 MySQL 数据库
   ↓
2. 启动 Spring Boot 后端 (http://localhost:8080)
   ↓
3. 启动 Android 模拟器
   ↓
4. 在 Android Studio 中运行 app
   ↓
5. 在 Android 应用中登录测试
```

## 截图说明

运行成功后，您将看到：

1. **登录页面** - 输入用户名和密码登录
2. **注册页面** - 点击"立即注册"创建新账号
3. **主页面** - 显示商品分类和商品列表
4. **底部导航** - 首页、收藏、发布、订单、我的
5. **商品详情页** - 显示商品图片、描述、价格等信息
6. **订单页面** - 显示订单列表和状态，支持筛选

## 主要功能

### 已完成功能

1. **用户模块**
   - 用户注册与登录
   - 个人信息管理
   - 修改密码
   - 用户头像上传

2. **商品模块**
   - 商品列表展示
   - 商品分类筛选
   - 商品搜索
   - 商品详情查看
   - 商品图片预览（支持多图切换）
   - 商品发布（含图片上传）
   - 商品上下架管理

3. **订单模块**
   - 订单创建
   - 订单状态管理
   - 订单倒计时显示
   - 订单详情查看
   - 修改收货地址和联系电话

4. **收藏模块**
   - 添加/取消收藏
   - 收藏列表展示

5. **聊天模块**
   - 买卖双方沟通

### UI优化

- 所有按钮添加 Ripple 点击效果
- 下拉刷新功能
- 自动加载下一页（无限滚动）
- 性能优化（RecyclerView视图复用、Glide图片缓存）

---

**记住：永远运行整个 app 模块，而不是单个 Java 文件！**
