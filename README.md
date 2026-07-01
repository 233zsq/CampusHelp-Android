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
| 🏠 **首页（接单大厅）** | 🚧 任务卡片列表（含状态标签：待接单/已接单/已完成/已取消/超时），筛选排序下拉刷新待补 |
| ✏️ **发布任务** | 🚧 发布表单（类型/标题/要求/报酬/地址/截止时间），预览 + 提交后端 `POST /api/tasks` |
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

> **提示**：登录 / 任务 / User / Credit 均依赖后端（纯网络）；Message 仍走 Room 兜底（待 C 迁移）。debug 包额外注入 `MockDataSeeder` 演示数据。

### 4. 配置后端地址

在 `app/build.gradle.kts` 中修改：

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://<your-server-ip>:8080/\"")
buildConfigField("String", "WS_BASE_URL", "\"ws://<your-server-ip>:8080/ws\"")
```

### 5. 部署后端到服务器（生产）

生产后端跑在阿里云**香港** ECS `47.239.124.167`（域名 `zsq-233.xin`，香港机器免 ICP 备案），对外入口是 **`https://zsq-233.xin`**（nginx 反代 443 → 本机 8080，Let's Encrypt 证书）。后端由 systemd 托管，**源码直接从本仓库 GitHub 拉取**——公开库，服务器匿名可 clone，无需手动 scp。本机连不上 GitHub 也没关系，服务器自己 pull，本机只负责 push。

**服务器布局**

| 路径 / 单位 | 说明 |
|---|---|
| `/opt/campushelp/repo` | `git clone` 自本仓库，部署时 `git fetch` + `checkout -B` 切到指定分支（默认 `main`） |
| `/opt/campushelp/deploy.sh` | 部署脚本：`[branch]` 参数 → fetch+checkout → 同步落地页 → 打包 → 换 jar → 重启 |
| `/opt/campushelp/app.jar` | 运行中的 fat jar（`-Dspring.profiles.active=prod`） |
| `/opt/campushelp/env` | 生产密钥（`MYSQL_PASSWORD` / `JWT_SECRET`），权限 600，不进仓库 |
| systemd `campushelp` | 开机自启 + 崩溃自动拉起（`Restart=always`），`After=mysql.service redis-server.service` |
| nginx（80/443） | 反代 → `127.0.0.1:8080`，`http` 301→`https`；`/ws` 带 WebSocket upgrade 头；配置 `/etc/nginx/sites-available/zsq-233.xin` |
| Let's Encrypt | 证书在 `/etc/letsencrypt/live/zsq-233.xin/`，certbot 申请 + systemd timer 自动续期 |

MySQL / Redis 都在本机（`127.0.0.1`），不对公网。安全组放行 `80` / `443`（nginx 对外入口）和 `8080`（后端直连，可关）；`3306` / `6379` 不对公网。

**日常部署（一条命令）**

```bash
# 1) 本地 push（经你的代理）
git push origin main

# 2) 服务器拉取 + 打包 + 重启（无参 = main）
ssh -i <your-key.pem> root@47.239.124.167 /opt/campushelp/deploy.sh
```

**云端测未合并的 server 分支**（不必先合 `main`）

```bash
# 1) 本地 push 功能分支（不合 main）
git push origin <feature-branch>

# 2) 服务器部署该分支
ssh -i <your-key.pem> root@47.239.124.167 /opt/campushelp/deploy.sh <feature-branch>

# 3) 验完切回生产 main
ssh -i <your-key.pem> root@47.239.124.167 /opt/campushelp/deploy.sh
```

> 测分支期间 prod URL 临时跑该分支代码（同一实例/端口）；若该分支 `site/index.html` 与 `main` 不同会一并覆盖落地页，切回 `main` 即恢复。回滚靠 `app.jar.bak.<时间戳>`（见下）。

`deploy.sh` 内部依次执行：`BRANCH="${1:-main}"` → `cd /opt/campushelp/repo && git fetch origin && git checkout -B "$BRANCH" "origin/$BRANCH"` → 同步 `site/index.html`（存在则 cp）→ `cd server && mvn -q clean package -DskipTests` → 备份旧 jar 为 `app.jar.bak.<时间戳>` → `cp target/campushelp-server-1.0.0.jar /opt/campushelp/app.jar` → `systemctl restart campushelp` → 打印 `systemctl is-active` 与最近日志。

> 脚本默认拉 `main`（无参），传分支名则 `fetch` + `checkout -B` 切到该分支并强制对齐远程——所以**云端测 server WIP 不必先合 `main`**，push 功能分支后 `deploy.sh <branch>` 即可上云，验完无参切回。覆盖 `app.jar` 时旧进程仍跑旧 jar（已加载进内存），`restart` 后才切到新的，无中间态。

**常用运维命令（服务器上）**

```bash
systemctl status campushelp        # 状态 + 最近日志
systemctl restart campushelp       # 手动重启
journalctl -u campushelp -f        # 实时日志
journalctl -u campushelp -n 50     # 最近 50 行
```

**回滚**：每次部署都留 `app.jar.bak.<时间戳>`，恢复最近一个备份即可：

```bash
cp /opt/campushelp/app.jar.bak.<时间戳> /opt/campushelp/app.jar
systemctl restart campushelp
```

**域名 + HTTPS（nginx + Let's Encrypt，一次性配置）**

域名 `zsq-233.xin` A 记录指向 ECS `47.239.124.167`（阿里云**香港**机器，免 ICP 备案）。对外入口为 `https://zsq-233.xin`，nginx 反代 443 → 本机 8080，`http` 自动 301 跳 `https`。首次配置（服务器上）：

```bash
# 1) 装 nginx + certbot
apt update && apt install -y nginx certbot python3-certbot-nginx

# 2) 写反代配置（80 → 8080，/ws 带 WebSocket upgrade 头）
cat > /etc/nginx/sites-available/zsq-233.xin <<'EOF'
server {
    listen 80;
    server_name zsq-233.xin;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    location /ws {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 86400s;
    }
}
EOF
ln -sf /etc/nginx/sites-available/zsq-233.xin /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

# 3) 申请证书 + 自动配 443 + HTTP→HTTPS 跳转（需安全组已放行 80/443）
certbot --nginx -d zsq-233.xin --non-interactive --agree-tos --register-unsafely-without-email --redirect
```

证书在 `/etc/letsencrypt/live/zsq-233.xin/`，certbot 的 systemd timer 自动续期（证书 90 天到期，无需手动）。`/ws` 的 upgrade 头已配好，等后端 WebSocket 端点实现后 `wss://` 直接通。

配好后 App 的地址（`app/build.gradle.kts`）：

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://zsq-233.xin/\"")
buildConfigField("String", "WS_BASE_URL", "\"wss://zsq-233.xin/ws\"")
```

## 数据库表

> 所有时间字段统一为 `BIGINT` 毫秒时间戳；所有表均含 `deleted` 逻辑删除标记。

### 1. `user` — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 用户 ID |
| `student_id` | VARCHAR(32) | UNIQUE NOT NULL | 学号，登录凭证 |
| `password` | VARCHAR(128) | NOT NULL | BCrypt 加密密码，不可逆 |
| `name` | VARCHAR(64) | DEFAULT '' | 昵称 / 真实姓名 |
| `avatar` | VARCHAR(256) | DEFAULT '' | 头像 URL（上传后回写） |
| `credit_score` | INT | DEFAULT 600 | 信用分，范围 0~1000，与 `credit_record` 求和同步 |
| `phone` | VARCHAR(20) | DEFAULT '' | 手机号（预留） |
| `created_at` | BIGINT | DEFAULT 0 | 注册时间戳(ms) |
| `deleted` | TINYINT | DEFAULT 0 | 逻辑删除：0=正常 / 1=已删除 |

### 2. `task` — 任务表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 任务 ID |
| `publisher_id` | BIGINT | NOT NULL | 发布者，关联 `user.id`（后端从 JWT 取，客户端传入被覆盖防越权） |
| `type` | TINYINT | DEFAULT 0 | 任务类型：0=跑腿 / 1=拼单 / 2=二手 |
| `title` | VARCHAR(128) | NOT NULL | 任务标题（必填） |
| `content` | TEXT | — | 任务要求 / 详细描述 |
| `reward` | DOUBLE | DEFAULT 0 | 报酬金额（元），0=面议 |
| `location` | VARCHAR(128) | DEFAULT '' | 地点文案（如"北区宿舍 3 号楼"） |
| `latitude` | DOUBLE | DEFAULT 0 | 纬度（地图定位） |
| `longitude` | DOUBLE | DEFAULT 0 | 经度（地图定位） |
| `status` | TINYINT | DEFAULT 0 | 0=待接单 / 1=已接单 / 2=已完成 / 3=已取消 |
| `deadline` | BIGINT | DEFAULT 0 | 接单截止时间戳(ms)，超时未接单前端标红 |
| `created_at` | BIGINT | DEFAULT 0 | 发布时间戳(ms) |
| `deleted` | TINYINT | DEFAULT 0 | 逻辑删除 |

**状态流转：**
```
待接单(0) ──接单──▶ 已接单(1) ──完成──▶ 已完成(2)
    │                                    │
    └──────────── 取消 ──────────────────▶ 已取消(3)
```

### 3. `order` — 接单表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 接单记录 ID |
| `task_id` | BIGINT | NOT NULL | 关联 `task.id`（同一任务同一时间最多一条进行中） |
| `taker_id` | BIGINT | NOT NULL | 接单人，关联 `user.id` |
| `accepted_at` | BIGINT | DEFAULT 0 | 接单时间戳(ms) |
| `deadline` | BIGINT | DEFAULT 0 | 完成截止时间戳(ms)，超时扣信用分 |
| `status` | TINYINT | DEFAULT 0 | 0=进行中 / 1=已完成 / 2=超时 |
| `completed_at` | BIGINT | DEFAULT 0 | 实际完成时间戳(ms) |
| `deleted` | TINYINT | DEFAULT 0 | 逻辑删除 |

**与 task 联动：** 接单 → 创建 order + 更新 task.status=1；完成/超时 → 更新 order.status + 更新 task.status + 触发信用分变动。

### 4. `chat_message` — 聊天消息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 消息 ID |
| `conversation_id` | VARCHAR(128) | NOT NULL | 会话 ID（双方 userId 按大小拼接，或关联 taskId） |
| `sender_id` | BIGINT | NOT NULL | 发送者，关联 `user.id` |
| `receiver_id` | BIGINT | NOT NULL | 接收者，关联 `user.id` |
| `content` | TEXT | — | 消息内容（文本正文 / 图片 URL / 订单卡片 JSON） |
| `type` | TINYINT | DEFAULT 0 | 0=文本 / 1=图片 / 2=订单卡片 |
| `timestamp` | BIGINT | DEFAULT 0 | 发送时间戳(ms) |
| `read` | TINYINT | DEFAULT 0 | 已读标记：0=未读 / 1=已读 |
| `deleted` | TINYINT | DEFAULT 0 | 逻辑删除 |

**消息流转：** 发送 → REST 落库 → WebSocket 推送在线接收方；接收方进聊天页 GET 拉历史 + 标记已读。

### 5. `credit_record` — 信用分变动记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 记录 ID |
| `user_id` | BIGINT | NOT NULL | 关联 `user.id` |
| `delta` | INT | DEFAULT 0 | 变动值：正=加分 / 负=减分 |
| `reason` | VARCHAR(256) | DEFAULT '' | 变动原因（如"任务完成奖励"、"接单超时扣分"） |
| `timestamp` | BIGINT | DEFAULT 0 | 变动时间戳(ms) |
| `deleted` | TINYINT | DEFAULT 0 | 逻辑删除 |

**同步机制：** `user.creditScore = SUM(delta) GROUP BY user_id`，每次 addRecord 后自动重算并 clamp 到 0~1000。

**典型场景：** 完成任务 +10 · 被接单 +5 · 接单超时 -10 · 恶意取消 -20

### 表关系简图

```
user ──1:N──▶ task           (publisher_id)
user ──1:N──▶ order          (taker_id)
user ──1:N──▶ credit_record  (user_id)
user ──1:N──▶ chat_message   (sender_id / receiver_id)
task ──1:1──▶ order          (task_id)
```

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
| 🔐 **A** | 身份与信用 + 数据架构 | 下阶段任务全部完成 | UserManager · 信用真源 · User/Credit 网络仓库 · logout |
| 📋 **B** | 任务流端到端 | 🔴 握手点：接单通知 + 单主状态管理 | 接单→通知→完成/取消闭环 · OrderApi · "我发布的" |
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
1. ✅ **统一信用分真源（最高优先，B/D 显示都依赖）**（已完成）— 展示信用分统一调 `GET /api/users/{id}`（取 `creditScore`）；明细调 `GET /api/credits?userId=`；删掉前端对本地 `User.creditScore` 的双写。
2. ✅ **UserRepository / CreditRepository 迁移到网络**（已完成）— 仿 `TaskRepository` 纯网络写法，补全 `UserApi`（getUser / updateUser）、`CreditApi`（list / sum / add）。
3. ✅ **UserManager 封装**（已完成）— 封 `TokenManager` + `UserApi`：`getCurrentUserId()`、`getUserInfo()`、`refreshUserInfo()`，给 B/C/D 统一入口。
4. ✅ **后端补身份便捷接口**（已完成）— `POST /api/auth/logout`（清 Redis token，踢人机制已就绪只差接口）；（可后置）refresh token。
5. ✅ **清理**（已完成）— `MockDataSeeder` 限 debug build。

**产出物**：`UserManager.getCurrentUserId()/getUserInfo()` · 信用分单一真源 + 加减明细（端到端）· User/Credit 网络仓库

**依赖/阻塞**：A **不再阻塞** B/C/D；`UserManager.getUserInfo()` 已就绪，D（个人中心 `MineFragment`）已对接，B（任务详情显示发布者）待对接。

</details>

<details>
<summary><b>📋 成员 B · 任务流端到端</b> —— 展开查看任务明细</summary>

**负责范围**：任务 CRUD、列表筛选排序、发布表单、任务详情、接单/完成/取消状态流转、Order 域端到端、后端 task/order 便捷接口。

> 🔴 **已到达握手点** — 发布与接单基础链路已打通，需要 C（消息通知）和 D（个人中心）配合完成跨模块联动。

**当前地基（已完成）**
- `Task` / `Order` 实体、`TaskDao` / `OrderDao`、`TaskRepository`（纯网络）
- `TaskApi`（GET 分页 / `?type=` / `?status=` / POST / PUT status）
- `HomeFragment` 已绑 `TaskRepository.observeAll()` + `TaskAdapter`（含状态标签：待接单/已接单/已完成/已取消/超时）
- `PublishFragment` 发布表单（类型/标题/要求/报酬/地址/截止时间，预览 + 提交后端）
- `TaskDetailActivity` 任务详情页（全部字段展示 + 接单按钮智能状态）
- 后端 `/api/tasks`、`/api/orders` 全就绪
- 缺：`OrderApi`、筛选 UI、DiffUtil

**握手点 — 需跨模块联动**

| # | 需求 | 涉及模块 | 说明 |
|:--|:-----|:---------|:-----|
| 1 | **接单时通知发布者** | B + C | 接单人点击接单 → `POST /api/orders` 创建订单 → 后端自动发一条 `chat_message`（type=2 订单卡片）给任务发布者，内容包含接单人信息 + 任务摘要；前端 C 的 MessageFragment 展示未读消息 |
| 2 | **单主查看"我发布的"** | B + D | 个人中心新增「我发布的」入口，调 `GET /api/tasks?publisherId={currentUserId}` 展示发布者自己的任务列表，每条显示当前状态（待接单/已接单/已完成/已取消），点击进入详情 |
| 3 | **单主更新接单状态** | B | 在"我发布的"列表中，已接单的任务提供「确认完成」和「取消订单」操作，调 `PUT /api/tasks/{id}/status` 更新 task 状态 + `PUT /api/orders/{id}/complete` 完结订单，触发信用分变动 |

**下阶段任务**
1. **OrderApi 对接** — 建 `OrderApi`（POST / GET `?takerId=` / PUT `/{id}/complete`），接单按钮从测试弹窗改为真实 API 调用。
2. **接单通知（联动 C）** — 接单成功后调消息接口通知发布者；后端 `POST /api/orders` 返回时自动创建通知消息。
3. **单主任务管理（联动 D）** — "我发布的"列表 + 确认完成 / 取消订单操作。
4. **列表体验升级** — `ListAdapter` + `DiffUtil` + 筛选 Tab + SwipeRefreshLayout。

**产出物**：接单→通知→完成/取消 全流程闭环 · "我发布的"任务管理 · `OrderApi`

**依赖**：C 的消息通知 + D 的个人中心入口；`UserManager.getUserInfo()`（A）。

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

- **第一波（已完成）**：✅ A 登录态 · ✅ B 发布+列表+详情 · C 后端 WS 待做 · D 高德 SDK 待做。
- **🔴 握手点**：B 接单通知（需 C 消息）· B 单主状态管理（需 D 个人中心）· C WS 握手（需 A 确认 token）· D 地图 Marker（需 B 任务数据）。
- **第二波（握手后）**：B 接单闭环 + 通知 · C 聊天页 + 会话列表 · D 个人中心 + 头像上传 · A UserManager 被各方消费。
