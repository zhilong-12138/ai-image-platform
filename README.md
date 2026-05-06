# AI 图片生成平台 - 完整项目

这是一个基于 Vue3 + Spring Boot 的现代化 AI 图片生成平台，包含用户前端、管理后台和后端 API 三个独立服务。

## 项目结构

```
ai-image-platform-v2/
├── backend/                 # Spring Boot 后端服务 (端口 8082)
│   ├── src/
│   │   ├── main/java/com/aiimage/
│   │   │   ├── entity/          # 数据库实体类
│   │   │   ├── mapper/          # MyBatis Plus Mapper
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── controller/      # API 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   └── AiImagePlatformApplication.java
│   │   └── resources/
│   │       ├── application.yml  # 应用配置
│   │       └── init.sql         # 数据库初始化脚本
│   └── pom.xml
│
├── ai-art-platform/           # Vue3 用户前端 (端口 5173)
│   ├── src/
│   ├── package.json
│   └── vite.config.js
│
├── frontend-admin/          # Vue3 管理后台 (端口 8081)
│   ├── src/
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

## 技术栈

### 后端
- **Java 17** + **Spring Boot 3.2**
- **MyBatis Plus** - ORM 框架
- **MySQL 8** - 关系数据库
- **Redis** - 缓存和会话存储
- **Druid** - 数据库连接池
- **Knife4j** - API 文档 (Swagger UI)
- **JWT** - 身份认证
- **Aliyun OSS** - 对象存储

### 前端
- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端

## 快速开始

### 1. 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库初始化
```bash
# 使用 MySQL 客户端执行初始化脚本
mysql -u root -p < backend/src/main/resources/init.sql
```

### 3. 配置修改
编辑 `backend/src/main/resources/application.yml`，修改以下配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_image_db
    username: root
    password: your-password
  redis:
    host: localhost
    port: 6379
  mail:
    username: your-email@qq.com
    password: your-app-password

app:
  jwt:
    secret: your-secret-key-change-in-production
  oss:
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
```

### 4. 启动后端服务
```bash
cd backend
mvn clean install
mvn spring-boot:run
# 后端服务运行在 http://localhost:8082/api
# API 文档访问 http://localhost:8082/api/doc.html
```

### 5. 启动前端服务
```bash
# 用户前端
cd  ai-art-platform 
npm install
npm run dev
# 访问 http://localhost:8080

# 管理后台
cd frontend-admin
npm install
npm run dev
# 访问 http://localhost:8081
```

### 6. Nginx 反向代理配置
```nginx
server {
    listen 80;
    server_name localhost;

    # 用户前端
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 管理后台
    location /admin/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://localhost:8082/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 核心功能

### 用户前端
- ✅ 邮箱验证码登录
- ✅ 提示词模板浏览
- ✅ AI 图片生成
- ✅ 生成记录管理
- ✅ 图片收藏
- ✅ 个人中心

### 管理后台
- ✅ 提示词管理 (CRUD)
- ✅ 用户管理
- ✅ 积分管理
- ✅ 生成记录审核
- ✅ 系统配置
- ✅ 数据统计

### 后端 API
- ✅ 用户认证 (JWT)
- ✅ 提示词管理
- ✅ 生成任务提交
- ✅ 积分系统
- ✅ 文件上传 (OSS)
- ✅ 邮箱验证码

## 数据库设计

### 核心表
1. **sys_user** - 用户表
2. **prompt** - 提示词表
3. **generation_record** - 生成记录表
4. **point_log** - 积分日志表
5. **favorite** - 收藏表
6. **email_code** - 邮箱验证码表
7. **system_config** - 系统配置表

详见 `backend/src/main/resources/init.sql`

## API 文档

启动后端服务后，访问 Knife4j UI：
```
http://localhost:8082/api/doc.html
```

## 开发指南

### 后端开发
1. 在 `entity` 包中定义数据模型
2. 在 `mapper` 包中创建 MyBatis Plus Mapper
3. 在 `service` 包中实现业务逻辑
4. 在 `controller` 包中暴露 API 接口
5. 使用 Knife4j 注解生成 API 文档

### 前端开发
1. 在 `src/views` 中创建页面组件
2. 在 `src/stores` 中定义 Pinia 状态
3. 在 `src/api` 中定义 API 调用函数
4. 在 `src/router` 中配置路由
5. 使用 Element Plus 组件构建 UI

## 部署

### Docker 部署
```bash
# 构建后端 Docker 镜像
cd backend
docker build -t ai-image-backend:1.0 .
docker run -d -p 8082:8082 ai-image-backend:1.0

# 构建前端 Docker 镜像
cd frontend-user
docker build -t ai-image-frontend:1.0 .
docker run -d -p 8080:8080 ai-image-frontend:1.0
```

### 生产环境建议
- 使用 HTTPS 加密通信
- 配置 CORS 跨域策略
- 启用 Redis 持久化
- 定期备份数据库
- 使用 CDN 加速静态资源
- 配置日志收集和监控

## 常见问题

### Q: 如何修改初始积分？
A: 编辑 `init.sql` 中的 `system_config` 表，修改 `init_points` 值。

### Q: 如何集成真实的 AI API？
A: 在 `GenerationController` 中实现 `submit` 方法，调用真实的 AI API（如 Stability AI）。

### Q: 如何处理大文件上传？
A: 使用 Aliyun OSS 的分片上传功能，在 `FileController` 中实现。

## 许可证

MIT License

## 支持

如有问题，请提交 Issue 或联系技术支持。
