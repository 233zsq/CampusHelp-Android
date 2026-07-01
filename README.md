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
| 🏠 **首页（接单大厅）** | 🚧 浏览任务列表（纯网络 TaskApi），支持跑腿/拼单/二手三类；筛选、排序、下拉刷新待补 |
| ✏️ **发布任务** | 📋 发布表单（类型/标题/内容/报酬/地点/截止），调 `POST /api/tasks` —— `PublishFragment` 当前为空壳 |
| 💬 **即时通讯** | 📋 自研 WebSocket（后端端点 + 前端连接待做）；当前仅有 REST 消息接口 + 前台保活空壳 + MessageBus |
| 👤 **个人中心** | 🚧 信用分仪表盘（Canvas 自绘 0~1000，分区间着色）+ 退出登录；资料/头像/记录入口待补 |
| 🔐 **登录注册** | ✅ JWT 令牌认证，学号 + 密码注册/登录，`TokenManager` 持久化 + 登录闸门 |
| 📍 **地图定位** | 📋 高德地图 SDK（3dmap / location / search 依赖已注释），定位 + 逆地理 + 任务地图模式 |
| ⭐ **信用系统** | 🚧 后端已就绪（credit_record + user.creditScore 同步并 clamp 0~1000）；单一真源已对接（`UserManager` → `user.creditScore`），前端加减触发待对接 |

> 状态图例：✅ 已完成 · 🚧 部分完成 · 📋 未开始（详见下方 [团队分工](#团队分工)）

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

> **提示**：登录与任务列表依赖后端（纯网络）；User / Credit / Message 仍可走 Room + Mock 数据兜底，待统一迁移到网络。

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

## 团队分工

> **v2（重构后）** · 登录已闭环，4 人可并行。
> 原分工假设「A 先做完登录态才能解锁 B/C/D」——重构后该假设失效。
> 两条全局约定：**① 网络为唯一真源**（Room 降级为缓存）· **② 全栈领域负责**（每人 own 自己领域的后端缺口）。

### 🧭 架构变更速览

| 维度 | 原分工 | 重构后 |
|:---|:---|:---|
| 登录态 | A 待做，阻塞全员 | ✅ **已闭环**（JWT 后端 + LoginActivity + TokenManager + 闸门 + 启动恢复） |
| 任务数据源 | Room | ✅ TaskRepository 已纯网络（TaskApi） |
| User/Credit/Message 数据源 | Room | User/Credit ✅ 已纯网络（仿 TaskRepository）；Message ⚠️ 仍 Room（C 待迁） |
| 后端 | 不存在 | ✅ Spring Boot 已部署（`47.239.124.167`），auth/task/order/credit/message REST 就绪；缺 WS / 上传 / 登出 / 便捷接口 |
| 信用分真源 | 本地双写 | ✅ 后端已同步 `user.creditScore` ↔ `credit_record` sum 并 clamp 0-1000，前端只消费 |
| IM 方案 | WebSocket / 环信 二选一 | 🎯 定为自研 WS，但后端没 WS → C own 后端端点 |
| 头像上传 | 前端做 | 🎯 后端没上传端点 → D own 后端端点 |

### 📌 全局约定（所有人遵守）

1. **网络为唯一真源** — Task 已是；User/Credit/Message 由各自 owner 迁移；Room 仅保留消息缓存（C 定），其余降级或删除；`MockDataSeeder` 限 debug build。
2. **全栈领域负责** — A=身份/信用便捷接口，B=任务/接单便捷接口，C=WebSocket+会话/未读，D=文件上传；每人端到端 own。
3. **身份统一入口** — `currentUserId` 走 `TokenManager`；用户信息走 A 的 `UserManager`。

### 👥 分工总览

| 成员 | 领域 | 当前阶段 | 关键产出 |
|:---:|:---|:---|:---|
| 🔐 **A** | 身份与信用 + 数据架构 | User/Credit 已迁网络 + UserManager 就绪 | UserManager · 信用真源 · User/Credit 网络仓库 |
| 📋 **B** | 任务流端到端 | 列表已绑网络，补体验+详情+接单 | 发布/接单/完成 · TaskDetailActivity · OrderApi |
| 💬 **C** | 即时通讯端到端（含后端 WS） | WS 空壳，从后端端点开始 | 后端 WS · 真连接 · ChatActivity · 会话列表 |
| 🗺️ **D** | 地图 + 个人中心端到端（含后端上传） | 信用盘占位，引入高德 | 高德 SDK · 地图模式 · 个人中心 · 头像上传 · 图表 |

<details>
<summary><b>🔐 成员 A · 身份与信用 + 数据架构</b> —— 展开查看任务明细</summary>

**负责范围**：全局登录态/身份基础设施（维护）、信用分体系端到端、User/Credit 数据源迁移到网络、后端身份/信用便捷接口。

> A 原来的阻塞活（登录）已完成，pivot 到「理顺数据架构」——User/Credit 仓库现已迁移到纯网络（与 TaskRepository 路线一致），UserManager 统一入口已就绪。

**当前地基（已完成）**
- `LoginActivity` 调 `/api/auth/login` + `/register`，JWT 持久化（`TokenManager`）+ `OkHttpProvider.setToken`
- `MainActivity` 登录闸门 + `CampusHelpApp` 启动恢复 token
- `UserApi` / `ApiResponse` / `TokenInterceptor` / `RetrofitClient`
- 后端 `/api/auth/*`、`/api/users/{id}`、`/api/credits`(+`/sum`) 就绪；`CreditServiceImpl` 已同步 creditScore 与 sum 并 clamp 0-1000
- `UserRepository` / `CreditRepository` 已迁移到纯网络（仿 `TaskRepository`，补 `UserApi.updateUser` / `CreditApi.sum`）

**下阶段任务（建议顺序）**
1. **统一信用分真源（最高优先，B/D 显示都依赖）** — 展示信用分统一调 `GET /api/users/{id}`（取 `creditScore`）；明细调 `GET /api/credits?userId=`；删掉前端对本地 `User.creditScore` 的双写。
2. ✅ **UserRepository / CreditRepository 迁移到网络**（已完成）— 仿 `TaskRepository` 纯网络写法，补全 `UserApi`（getUser / updateUser）、`CreditApi`（list / sum / add）。
3. ✅ **UserManager 封装**（已完成）— 封 `TokenManager` + `UserApi`：`getCurrentUserId()`、`getUserInfo()`、`refreshUserInfo()`，给 B/C/D 统一入口。
4. **后端补身份便捷接口** — `POST /api/auth/logout`（清 Redis token，踢人机制已就绪只差接口）；（可后置）refresh token。
5. **清理** — `MockDataSeeder` 限 debug build。

**产出物**：`UserManager.getCurrentUserId()/getUserInfo()` · 信用分单一真源 + 加减明细（端到端）· User/Credit 网络仓库

**依赖/阻塞**：A **不再阻塞** B/C/D；`UserManager.getUserInfo()` 已就绪，D（个人中心 `MineFragment`）已对接，B（任务详情显示发布者）待对接。

</details>

<details>
<summary><b>📋 成员 B · 任务流端到端</b> —— 展开查看任务明细</summary>

**负责范围**：任务 CRUD、列表筛选排序、发布表单、任务详情、接单/完成/取消状态流转、Order 域端到端、后端 task/order 便捷接口。

> TaskRepository 已纯网络、HomeFragment 已能绑网络列表。重心从「搭列表」转到「补体验 + 详情/接单流程」。前端没有 OrderApi，B 要建。

**当前地基（已完成）**
- `Task` / `Order` 实体、`TaskDao` / `OrderDao`、`TaskRepository`（纯网络）
- `TaskApi`（GET 分页 / `?type=` / `?status=` / POST / PUT status）
- `HomeFragment` 已绑 `TaskRepository.observeAll()` + `TaskAdapter`
- 后端 `/api/tasks`、`/api/orders` 全就绪
- 缺：`OrderApi`、`TaskDetailActivity`、`PublishFragment` 逻辑、筛选 UI、DiffUtil

**下阶段任务（建议顺序）**
1. **修 MainActivity Tab 切换** — 现在 `FragmentTransaction` 可能每次 new Fragment 丢滚动/输入状态；改 `show/hide` 或 ViewPager2 + BottomNavigationView。
2. **列表体验升级** — `BaseAdapter.notifyDataSetChanged` → `ListAdapter` + `DiffUtil`；顶部筛选 Tab（全部/跑腿/拼单/二手 → `GET /api/tasks?type=`）+ 排序；loading / empty / SwipeRefreshLayout 下拉刷新。
3. **发布任务页** — 补全 `PublishFragment` 表单，提交调 `POST /api/tasks`（`publisherId` 后端强制覆盖为 currentUserId，前端不传）。
4. **OrderApi + 接单流程** — 建 `OrderApi`（POST / GET `?takerId=` / PUT `/{id}/complete`）；新建 `TaskDetailActivity`（详情 + 发布者信息调 A 的 `UserManager.getUserInfo()` + 信用分）；接单 `POST /api/orders` + `PUT /api/tasks/{id}/status=1`；完成/取消触发信用分加减（调 A 的 `POST /api/credits`）。
5. **后端补 task/order 便捷接口** — 我的发布 `GET /api/tasks?publisherId=`（现不支持，B 加参数）；报酬排序参数。

**产出物**：可发布/接单/完成/取消 · 首页筛选排序 + 刷新 + 空状态 · `TaskDetailActivity` · `OrderApi`

**依赖**：A 的 `currentUserId`（已就绪）+ `getUserInfo`（尽早对接）；地图 Marker +「附近」筛选依赖 D 的 `LocationHelper`；完成订单触发信用分调 A 的 `POST /api/credits`。

</details>

<details>
<summary><b>💬 成员 C · 即时通讯端到端（含后端 WebSocket）</b> —— 展开查看任务明细</summary>

**负责范围**：后端 WebSocket 端点、前端 `WebSocketService` 真连接/断线重连、`ChatActivity`、`MessageFragment` 会话列表、通知、消息数据源迁移。

> 后端没 WS，又定了自研 WS，所以 C 现在 own 后端 WS 端点。MessageRepository 也要从 Room 迁到「REST 历史 + WS 实时」。

**当前地基（已完成）**
- `ChatMessage` / `MessageDao` / `MessageRepository`（本地 Room）
- `MessageBus`（LiveData 总线）、`WebSocketService`（空壳，仅前台通知）
- `NotificationHelper`（IM + Service 渠道）
- `OkHttpProvider`（30s ping，为 WS 准备）、`BuildConfig.WS_BASE_URL = ws://47.239.124.167:8080/ws`
- 后端 `/api/messages`（发/查/标记已读）REST 就绪，**无 WS**

**下阶段任务（建议顺序）**
1. **后端 WebSocket（C owns）** — `server/pom.xml` 加 `spring-boot-starter-websocket`；`WebSocketHandler` 握手时从 token 解析 userId 绑定 session，维护 `userId → Session` 映射；收消息落库（复用 `MessageService.send`）+ 推接收方在线 session，离线只落库；消息格式 `{from, to, content, type, timestamp}`；端点对齐 `/ws`。
2. **前端 WebSocketService 真连接** — `OkHttpProvider.getClient().newWebSocket()` 连 `WS_BASE_URL`，握手带 token；`onMessage` 解析 → 写 Room 缓存 → `MessageBus.post` → 不在聊天页则发通知；指数退避断线重连 + START_STICKY + 前台保活；登录成功后 `startService`。
3. **聊天页** — 新建 `ChatActivity`，气泡 RecyclerView，发送走 WS（POST `/api/messages` 兜底），已读调 `PUT /api/messages/read`，进页面先 GET 拉历史。
4. **会话列表 + 未读** — `MessageFragment` 改造为会话列表（需后端 `GET /api/messages/conversations`，C 补）；底部 tab 未读角标（后端 `GET /api/messages/unread/count`，C 补）。
5. **通知** — 不在聊天页时收到 WS 消息 → 弹通知，点击跳 `ChatActivity`；Android 13+ 动态申请 `POST_NOTIFICATIONS`（`PermissionUtils` 已定义但无人调用，C 接起来）。
6. **MessageRepository 迁移** — 历史走 REST，实时走 WS，Room 保留作聊天记录缓存。

**产出物**：后端 WS 端点 + 前端真连接 · 两人实时聊天 + 历史 · 会话列表 + 未读角标 + 通知

**依赖**：A 的登录态（token + currentUserId，已就绪）；WS 握手需跟 A 确认 token 传递方式（query param 还是 header）。

</details>

<details>
<summary><b>🗺️ 成员 D · 地图 + 个人中心端到端（含后端文件上传）</b> —— 展开查看任务明细</summary>

**负责范围**：高德 SDK 集成/定位/逆地理、任务地图模式、个人中心页、头像上传、数据可视化图表、后端文件上传端点。

> 后端没上传端点，头像上传被阻塞，所以 D own 后端上传。MineFragment 现在硬编码 820 分，要改成调 A 的网络接口。

**当前地基（已完成）**
- `MineFragment`（CreditGaugeView 占位 + 退出登录）、`CreditGaugeView` / `EmptyView`
- 高德 SDK 依赖被注释、`PLACEHOLDER_AMAP_KEY` 待替换
- 头像/相机权限 + FileProvider 已配（`PermissionUtils` 已定义但无人调用）
- 后端 `User.avatar` 字段在，无上传端点；`PUT /api/users/{id}` 可回写 avatar URL
- 缺：高德 SDK、`LocationHelper`、地图模式、个人中心资料、头像上传、MPAndroidChart（未引入）

**下阶段任务（建议顺序）**
1. **引入高德 SDK** — 取消注释 3dmap / location / search；替换 `PLACEHOLDER_AMAP_KEY`；封装 `LocationHelper`（getCurrentLocation / latlngToAddress）；启动时 `registerForActivityResult` 接定位权限。
2. **任务地图模式** — 首页加「地图」Tab，高德展示周围 Marker（数据复用 B 的 `GET /api/tasks`）；点 Marker 弹卡片 → `TaskDetailActivity`（B 的产出）；「附近」筛选配合 B 的列表。
3. **个人中心页（数据走网络）** — `MineFragment`：头像/昵称/学号/信用分（调 A 的 `GET /api/users/{id}`，替换硬编码 820）；入口：我发布的（B 的 `GET /api/tasks?publisherId=`）、我接的（B 的 `GET /api/orders?takerId=`）、信用分明细（A 的 `GET /api/credits?userId=`，加分绿/减分红）；退出登录调 A 的 `POST /api/auth/logout` + 清 `TokenManager` + 停 C 的 `WebSocketService`。
4. **头像上传（D owns 后端）** — 后端加 `POST /api/upload`（MultipartFile，存本地磁盘或 OSS，返回 URL）；前端相册/相机（`registerForActivityResult` + `PermissionUtils` 媒体/相机权限），Glide 显示，上传后 `PUT /api/users/{id}` 回写 avatar URL。
5. **数据可视化** — 引入 MPAndroidChart；近 7 天接单/发单数、信用分变化曲线（数据来自 B 任务接口 + A 信用记录）。

**产出物**：高德地图看附近任务 · 完整个人中心（网络数据）· 头像上传端到端 · 图表

**依赖**：A 的登录态 + `getUserInfo` + 信用分明细；B 的任务/接单数据（地图 Marker + 我发布的/接的）；头像上传后端端点 D 自己补。

</details>

### ⏱️ 并行节奏

- **第一波（立即并行，互不阻塞）**：A 做信用真源 + UserManager · B 做列表升级 + 发布页 · C 做后端 WS · D 做高德 SDK。
- **汇合点**：B 的 `TaskDetailActivity` ← A 的 `getUserInfo` · D 的个人中心 ← A+B 的接口 · C 的 WS 握手 ← A 确认 token 传递方式 · D 的地图 Marker ← B 的任务数据。
