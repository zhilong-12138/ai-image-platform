# AI 图片生成平台 - 部署指南

本指南介绍如何部署 AI 图片生成平台到生产环境。

## 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 或者 Java 17、Node.js 18、MySQL 8、Redis 6

## 方式 1：使用 Docker Compose（推荐）

### 1. 修改配置

编辑 `backend/src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/ai_image_db
    username: aiimage
    password: aiimage123
  redis:
    host: redis
    port: 6379
  mail:
    username: your-email@qq.com
    password: your-app-password

app:
  jwt:
    secret: your-production-secret-key-change-this
  oss:
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
```

### 2. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 3. 验证服务

- 用户前端：http://localhost:8080
- 管理后台：http://localhost:8081
- 后端 API：http://localhost:8082/api/doc.html
- Nginx 反向代理：http://localhost

## 方式 2：本地开发环境

### 1. 启动 MySQL 和 Redis

```bash
# 使用 Docker
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root123456 mysql:8.0
docker run -d -p 6379:6379 redis:7-alpine

# 初始化数据库
mysql -u root -p < backend/src/main/resources/init.sql
```

### 2. 启动后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 3. 启动前端服务

```bash
# 用户前端
cd frontend-user
npm install
npm run dev

# 管理后台（新终端）
cd frontend-admin
npm install
npm run dev
```

### 4. 启动 Nginx

```bash
# macOS
brew install nginx
nginx -c $(pwd)/nginx.conf

# Linux
sudo apt-get install nginx
sudo nginx -c $(pwd)/nginx.conf

# Windows
# 下载 Nginx，修改 conf/nginx.conf，运行 nginx.exe
```

## 生产环境配置

### SSL/TLS 证书

编辑 `nginx.conf`，取消注释 HTTPS 配置：

```nginx
server {
    listen 443 ssl http2;
    server_name example.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
}
```

### 环境变量

创建 `.env` 文件：

```env
MYSQL_ROOT_PASSWORD=your-secure-password
MYSQL_PASSWORD=your-secure-password
JWT_SECRET=your-production-secret-key
API_KEY=your-api-key
OSS_ACCESS_KEY_ID=your-access-key-id
OSS_ACCESS_KEY_SECRET=your-access-key-secret
```

### 监控和日志

```bash
# 查看容器日志
docker-compose logs -f backend

# 查看 Nginx 日志
docker-compose logs -f nginx

# 进入容器调试
docker-compose exec backend bash
```

## 性能优化

### 1. 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_user_email ON sys_user(email);
CREATE INDEX idx_generation_user ON generation_record(user_id);
CREATE INDEX idx_generation_status ON generation_record(status);

-- 启用查询缓存
SET GLOBAL query_cache_size = 268435456;
SET GLOBAL query_cache_type = 1;
```

### 2. Redis 配置

```bash
# 启用 Redis 持久化
redis-cli CONFIG SET save "900 1 300 10 60 10000"
redis-cli CONFIG REWRITE
```

### 3. Nginx 性能调优

```nginx
# 增加工作进程
worker_processes auto;

# 增加连接数
worker_connections 2048;

# 启用 HTTP/2
listen 443 ssl http2;

# 启用缓存
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=my_cache:10m;
```

## 故障排查

### 问题 1：数据库连接失败

```bash
# 检查 MySQL 是否运行
docker-compose ps mysql

# 检查连接字符串
docker-compose logs mysql
```

### 问题 2：前端无法访问后端 API

```bash
# 检查 Nginx 配置
docker-compose exec nginx nginx -t

# 检查代理转发
curl -v http://localhost/api/prompt/list
```

### 问题 3：内存不足

```bash
# 增加 Docker 内存限制
docker-compose down
# 编辑 docker-compose.yml，添加 mem_limit
docker-compose up -d
```

## 备份和恢复

### 备份数据库

```bash
# 备份 MySQL
docker-compose exec mysql mysqldump -u root -p ai_image_db > backup.sql

# 备份 Redis
docker-compose exec redis redis-cli BGSAVE
docker cp ai-image-redis:/data/dump.rdb ./redis-backup.rdb
```

### 恢复数据库

```bash
# 恢复 MySQL
docker-compose exec -T mysql mysql -u root -p < backup.sql

# 恢复 Redis
docker cp redis-backup.rdb ai-image-redis:/data/dump.rdb
docker-compose restart redis
```

## 监控和告警

### 使用 Prometheus 和 Grafana

```yaml
# docker-compose.yml 添加
prometheus:
  image: prom/prometheus
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana
  ports:
    - "3000:3000"
```

## 常见问题

**Q: 如何修改初始积分？**
A: 编辑 `backend/src/main/resources/init.sql`，修改 `system_config` 表中的 `init_points` 值。

**Q: 如何添加新的管理员用户？**
A: 直接在数据库中插入用户，设置 `role = 'admin'`。

**Q: 如何处理大文件上传？**
A: 配置 `nginx.conf` 中的 `client_max_body_size`，默认为 20M。

**Q: 如何扩展到多个服务器？**
A: 使用 Kubernetes 或 Docker Swarm，配置负载均衡和数据库主从复制。

## 支持

如有问题，请提交 Issue 或联系技术支持。
