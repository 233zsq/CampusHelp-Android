# CampusHelp 校园互助平台

一个基于 Android + Spring Boot 的校园互助平台，提供任务发布、接单、即时通讯和信用分系统，方便同学之间跑腿、拼单、二手交易等互助场景。

## 项目结构

```
CampusHelp/
├── app/                          # Android 客户端（Java + MVVM）
│   └── src/main/java/com/campus/help/
│       ├── core/                 # 基础设施层
│       │   ├── base/             # BaseActivity / BaseFragment / BaseViewModel 基类
│       │   ├── bus/              # MessageBus 事件总线
│       │   ├── db/               # Room 数据库（AppDatabase）
│       │   ├── network/          # Retrofit + OkHttp 网络层
│       │   ├── utils/            # 工具类（Token / 通知 / 权限 / 时间 / AMap 隐私合规）
│       │   └── widget/           # 自定义控件（CreditGaugeView 信用分仪表盘）
│       ├── data/                 # 数据层
│       │   ├── dao/              # Room DAO 接口
│       │   ├── model/            # 实体（User / Task / Order / ChatMessage / CreditRecord）
│       │   └── repo/             # Repository（含 MockDataSeeder 离线演示数据）
│       ├── feature/im/           # IM 模块
│       │   └── WebSocketService  # WebSocket 前台保活服务
│       └── ui/                   # 界面层
│           ├── MainActivity      # 底部导航壳（4 个 Tab）
│           ├── HomeFragment      # 首页 / 接单大厅
│           ├── PublishFragment   # 发布任务
│           ├── MessageFragment   # 消息
│           ├── MineFragment      # 个人中心（信用分仪表盘 + 退出登录）
│           └── login/            # 登录 / 注册
├── server/                       # Spring Boot 后端
│   └── src/main/java/com/campus/help/server/
│       ├── controller/           # REST 接口（Auth / Task / Order / Message / Credit / User）
│       ├── service/              # 业务逻辑层
│       ├── mapper/               # MyBatis-Plus Mapper
│       ├── entity/               # 实体类
│       ├── common/               # JWT / 统一响应
│       ├── config/               # MyBatis-Plus / Redis / WebMvc 配置
│       └── interceptor/          # JWT 鉴权拦截器
└── build.gradle.kts              # 根构建脚本
```

## 功能概览

| 模块 | 说明 |
|------|------|
| 🏠 **首页（接单大厅）** | 浏览任务列表，支持跑腿、拼单、二手三种类型，Room 本地 + 后端双通道 |
| ✏️ **发布任务** | 发布互助任务，填写标题、描述、报酬、地点、截止时间 |
| 💬 **即时通讯** | WebSocket 实时消息，支持文本 / 图片 / 订单卡片，前台服务保活 |
| 👤 **个人中心** | 信用分仪表盘（Canvas 自定义控件 0~1000 分，按区间着色）、退出登录 |
| 🔐 **登录注册** | JWT 令牌认证，学号 + 密码注册/登录 |
| 📍 **地图定位** | 高德地图 SDK 集成（预留），定位 + 地图选点 |
| ⭐ **信用系统** | 任务完成/超时/取消 → 信用分增减，可视化仪表盘展示 |

## 技术栈

### Android 客户端

| 类别 | 技术 |
|------|------|
| 语言 | Java 11 |
| 架构 | MVVM（ViewModel + LiveData + Repository） |
| 本地数据库 | Room |
| 网络 | Retrofit 2 + OkHttp（自带 WebSocket） |
| 图片加载 | Glide |
| UI | Material Design + ViewBinding + RecyclerView + ViewPager2 |
| 地图（预留） | 高德地图 3D Map / Location / Search SDK |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 16 (API 36) |

### 后端

| 类别 | 技术 |
|------|------|
| 语言 | Java 17 |
| 框架 | Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 认证 | JWT (jjwt 0.12.5) |
| 密码加密 | BCrypt (spring-security-crypto) |

## 快速开始

### 1. 克隆项目

```bash
git clone <repo-url>
cd CampusHelp
```

### 2. 启动后端

```bash
cd server
# 确保本地 MySQL 和 Redis 已启动
# 执行 server/src/main/resources/db/schema.sql 初始化数据库
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080/`。

### 3. 运行 Android 客户端

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 如需高德地图功能：在 `AndroidManifest.xml` 中替换 `PLACEHOLDER_AMAP_KEY` 为你在 [lbs.amap.com](https://lbs.amap.com) 申请的 Key
4. 选择模拟器或真机，点击 Run

> **提示**：未连接后端时，App 会自动使用 Room 本地数据库 + Mock 数据兜底运行。

### 4. 配置后端地址

在 `app/build.gradle.kts` 中修改：

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://<your-server-ip>:8080/\"")
buildConfigField("String", "WS_BASE_URL", "\"ws://<your-server-ip>:8080/ws\"")
```

## 数据库表

| 表名 | 说明 |
|------|------|
| `user` | 用户表（学号、密码 BCrypt、昵称、头像、信用分） |
| `task` | 任务表（类型 跑腿/拼单/二手、报酬、状态、地点坐标） |
| `order` | 接单表（关联任务、接单人、状态 进行中/已完成/超时） |
| `chat_message` | 聊天消息（会话ID、发送者、接收者、内容类型） |
| `credit_record` | 信用分变动记录（变动值、原因、时间） |

## 团队分工（拟使用）

| 成员 | 负责模块 |
|------|----------|
| 成员 A | 登录注册 + 信用分系统 |
| 成员 B | 首页任务列表 + 发布任务 |
| 成员 C | 即时通讯（WebSocket + 消息 Tab） |
| 成员 D | 个人中心 + 高德地图集成 |
