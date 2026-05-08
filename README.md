# AI 图片生成平台

基于 Vue3 + Spring Boot 的 AI 图片生成平台，包含用户前端、管理后台和后端 API 三个独立服务。

## 项目架构

```
ai-image-platform/
├── backend/              # Spring Boot 后端 (端口 8082, 上下文 /api)
├── frontend-user/        # Vue3 用户前端 (端口 8080)
├── frontend-admin/       # Vue3 管理后台 (端口 8081)
├── nginx.conf            # Nginx 反向代理配置
└── docker-compose.yml    # Docker 容器编排
```

## 技术栈

### 后端
- **Java 17** + **Spring Boot 3.2.4**
- **MyBatis Plus 3.5.5** - ORM 框架
- **MySQL 8** - 关系数据库
- **Redis 7** - 缓存和会话
- **Druid** - 数据库连接池
- **Knife4j 4.5.0** - API 文档 (访问 /api/doc.html)
- **JWT** - 身份认证
- **Aliyun OSS** - 对象存储

### 前端
- **Vue 3** + **Vite** + **TypeScript**
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Docker 20.10+ (可选)

### 方式一：Docker Compose 部署（推荐）

```bash
# 1. 修改配置
# 编辑 backend/src/main/resources/application.yml 配置数据库、Redis、OSS 等

# 2. 启动所有服务
docker-compose up -d

# 3. 验证服务 (生产模式)
# 用户前端: http://localhost:8080
# 管理后台: http://localhost:8081
# 后端 API:  http://localhost:8082/api/doc.html
# Nginx:     http://localhost
```

### 方式二：本地开发

```bash
# 1. 初始化数据库
mysql -u root -p < backend/src/main/resources/init.sql

# 2. 启动后端
cd backend
mvn clean install
mvn spring-boot:run

# 3. 启动用户前端 (端口 5173)
cd frontend-user
npm install
npm run dev

# 4. 启动管理后台 (端口 5277，新终端)
cd frontend-admin
npm install
npm run dev
```

## 服务端口

| 服务 | 开发端口 | Docker 端口 | 访问地址 |
|------|----------|-------------|----------|
| 用户前端 | 5173 | 8080 | http://localhost:5173 |
| 管理后台 | 5277 | 8081 | http://localhost:5277 |
| 后端 API | 8082 | 8082 | http://localhost:8082/api |
| API 文档 | - | - | http://localhost:8082/api/doc.html |
| Nginx | - | 80 | http://localhost |

## 核心功能

### 用户端
- 邮箱验证码登录/注册
- 提示词模板浏览
- AI 图片生成
- 生成记录管理
- 图片收藏
- 个人中心与积分管理

### 管理后台
- 提示词管理 (CRUD)
- 用户管理
- 积分管理
- 生成记录审核
- 系统配置
- 数据统计

### 后端 API
- JWT 用户认证
- 提示词管理
- 异步生成任务
- 积分系统
- 文件上传 (OSS)
- 邮箱验证码

## 目录结构

### 后端 (backend/src/main/java/com/aiimage/)
```
├── aspect/          # AOP 切面
├── config/          # 配置类 (JWT、Swagger、Web)
├── controller/      # REST API 控制器
├── dto/              # 数据传输对象
├── entity/           # 数据库实体
├── mapper/           # MyBatis Plus Mapper
├── service/         # 业务逻辑层
├── task/            # 异步任务
└── util/            # 工具类 (JWT、OSS、邮件)
```

### 前端
```
frontend-user/src/
├── api/             # API 调用
├── components/      # 公共组件
├── stores/          # Pinia 状态
├── views/           # 页面组件
├── styles/          # 全局样式
└── App.vue          # 根组件

frontend-admin/src/
└── ... (同上)
```

## 数据库表

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| prompt | 提示词模板表 |
| generation_record | 生成记录表 |
| point_log | 积分日志表 |
| favorite | 收藏表 |
| email_code | 邮箱验证码表 |
| system_config | 系统配置表 |

详见 `backend/src/main/resources/init.sql`

## 配置说明

### application.yml 敏感字段
```yaml
spring:
  datasource:
    password: your-db-password
  redis:
    password: your-redis-password
  mail:
    username: your-email@qq.com
    password: your-app-password

app:
  jwt:
    secret: your-production-secret-key
  oss:
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
```

## Nginx 配置

通过 nginx.conf 配置反向代理：
- `/` → 用户前端 (8080)
- `/admin` → 管理后台 (8081)
- `/api` → 后端 API (8082)

## 常见问题

**Q: 如何修改初始积分？**
A: 编辑 `backend/src/main/resources/init.sql` 中的 `system_config` 表。

**Q: 如何添加管理员？**
A: 在数据库中插入用户记录，设置 `role = 'admin'`。

**Q: 如何处理大文件上传？**
A: 修改 `nginx.conf` 中的 `client_max_body_size`（默认 20M）。

## 相关文档

- [部署指南](DEPLOYMENT.md) - 完整的生产环境部署说明
- [CLAUDE.md](CLAUDE.md) - 开发指南
- [API 文档](http://localhost:8082/api/doc.html) - 启动后端后访问

## License

MIT
