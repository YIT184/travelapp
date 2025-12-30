# TravelApp - 旅行应用

一个基于Android平台的旅行分享应用，提供地图导航、旅行分享和用户中心等功能。

## 项目概述

TravelApp 是一款专为旅行爱好者设计的移动应用，用户可以在应用中分享旅行照片、查看地图信息、编辑个人资料等。应用采用现代化的 Material3 设计风格，提供流畅的用户体验。

## 功能特点

### 1. 首页功能
- 展示旅行相关内容
- 图片轮播展示
- 内容分类浏览

### 2. 地图功能
- 基于高德地图SDK的地图显示
- 定位服务
- 地点搜索
- 周边设施查询（停车场、厕所等）

### 3. 发布功能
- 分享旅行照片
- 编辑发布内容
- 图片选择与上传

### 4. 用户中心
- 个人资料管理
- 编辑个人信息
- 查看我的帖子
- 登录/注册功能

## 技术栈

- **开发语言**：Java + Kotlin
- **UI框架**：Android Material3
- **地图服务**：高德地图SDK
- **网络请求**：Retrofit
- **架构模式**：MVVM（Model-View-ViewModel）
- **构建工具**：Gradle

## 项目结构

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/edu/travels/travelapp/
│   │   │   │   ├── di/              # 依赖注入相关
│   │   │   │   ├── model/           # 数据模型
│   │   │   │   ├── ui/              # UI组件
│   │   │   │   │   ├── home/        # 首页相关
│   │   │   │   │   ├── map/         # 地图相关
│   │   │   │   │   ├── publish/     # 发布相关
│   │   │   │   │   └── user/        # 用户中心相关
│   │   │   │   ├── MainActivity.java
│   │   │   │   └── MyApplication.java
│   │   │   └── res/                 # 资源文件
│   │   │       ├── drawable/        # 图片资源
│   │   │       ├── layout/          # 布局文件
│   │   │       ├── menu/            # 菜单资源
│   │   │       └── values/          # 配置文件
│   │   └── AndroidManifest.xml      # 应用配置
│   └── build.gradle.kts             # 模块构建配置
├── gradle/                          # Gradle配置
└── settings.gradle.kts              # 项目设置
```

## 安装说明

### 前提条件
- Android Studio Hedgehog 或更高版本
- JDK 17 或更高版本
- Android SDK 34 或更高版本
- 模拟器或真实设备（Android 8.0+）

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/YIT184/travelapp.git
   ```

2. **打开项目**
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 导航到项目目录并选择

3. **同步 Gradle**
   - 等待 Android Studio 自动同步 Gradle
   - 如未自动同步，点击 "Sync Now" 按钮

4. **构建项目**
   - 点击 "Build" -> "Make Project" 或使用快捷键 `Ctrl+F9`

5. **运行应用**
   - 连接模拟器或真实设备
   - 点击 "Run" -> "Run 'app'" 或使用快捷键 `Shift+F10`

## 应用权限

应用需要以下权限：

- `INTERNET` - 网络访问
- `ACCESS_NETWORK_STATE` - 网络状态获取
- `ACCESS_WIFI_STATE` - WIFI状态获取
- `ACCESS_FINE_LOCATION` - 精确定位
- `ACCESS_COARSE_LOCATION` - 粗略定位
- `READ_PHONE_STATE` - 读取设备状态
- `WRITE_EXTERNAL_STORAGE` - 外部存储写入

## 高德地图配置

应用集成了高德地图SDK，使用前需要在 `AndroidManifest.xml` 中配置高德API Key：

```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="YOUR_API_KEY" />
```

## 主要页面说明

### 1. 主页面（MainActivity）
- 底部导航栏，包含首页、地图、个人中心三个选项
- 支持 Fragment 切换
- 应用入口点

### 2. 首页（HomeFragment）
- 展示旅行相关内容
- 图片网格布局
- 内容分类筛选

### 3. 地图页面（MapFragment）
- 高德地图显示
- 定位功能
- 地点搜索
- 周边设施查询

### 4. 发布页面（PublishFragment）
- 图片选择与上传
- 内容编辑
- 发布按钮

### 5. 用户中心（UserFragment）
- 个人资料展示
- 我的帖子
- 编辑资料入口

## 开发说明

### 网络请求

应用使用 Retrofit 进行网络请求，配置文件位于 `di/RetrofitClient.java` 和 `di/ApiService.java`。

### 数据模型

- `dto/` 目录：数据传输对象
- `vo/` 目录：视图对象

### 样式设计

应用采用 Material3 设计风格，主题配置位于 `res/values/themes.xml`。

## 注意事项

1. **高德地图API Key**：请确保在 `AndroidManifest.xml` 中配置了有效的高德API Key
2. **定位权限**：在使用地图功能前，需要确保已授予定位权限
3. **网络连接**：部分功能需要网络连接才能正常使用
4. **存储权限**：发布功能需要存储权限来选择图片


## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，欢迎联系：
- 邮箱：3620971624@qq.com
- GitHub：https://github.com/YIT184

---

© 2025 TravelApp. All rights reserved.
