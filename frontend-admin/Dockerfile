# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /build

# 复制 package.json 和源代码
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# 构建项目
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 复制 Nginx 配置
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制构建好的静态文件
COPY --from=builder /build/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
