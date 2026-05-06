# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI 图片生成平台 - Vue3 + Spring Boot application with three services. Uses MySQL 8, Redis, JWT auth, and Aliyun OSS.

## Architecture

```
ai-image-platform-v2/
├── backend/           # Spring Boot 3.2 API (port 8082, context /api)
├── ai-art-platform/   # Vue3 user-facing app (port 8080)
├── frontend-admin/    # Vue3 admin dashboard (port 8081)
├── nginx.conf          # Reverse proxy: / → user, /admin → admin, /api → backend
└── docker-compose.yml # All services containerized with MySQL/Redis
```

## Common Commands

```bash
# Backend
cd backend && mvn clean install                    # Build
cd backend && mvn spring-boot:run                  # Run (port 8082)
cd backend && mvn test                              # Run tests
cd backend && mvn test -Dtest=UserServiceTest       # Run single test

# Frontends
cd ai-art-platform && npm install && npm run dev   # User frontend (port 8080)
cd frontend-admin && npm install && npm run dev     # Admin frontend (port 8081)

# Docker
docker-compose up -d                # Start all services
docker-compose logs -f              # Follow logs
docker-compose down                 # Stop all
```

## Backend Structure

Package `com.aiimage`:
- `entity/` — MyBatis Plus entities
- `mapper/` — MyBatis Plus Mapper interfaces
- `service/impl/` — Business logic
- `controller/` — REST endpoints (under `/api` context)
- `dto/` — ApiResponse<T> wrappers
- `util/` — JwtUtil, EmailUtil, OssUtil, AsyncImageGenerator
- `config/` — JwtInterceptor, WebConfig, SwaggerConfig

## Key Patterns

### Authentication (Three-Flow System)
1. **Login**: `POST /user/login` — email + email code → JWT token
2. **Register**: `POST /user/register` — email + code + password + inviteCode? → JWT + registration bonus (100 credits) + invite reward if applicable
3. **Password Reset**: `POST /user/sendCode` (with type=reset) → `POST /user/resetPassword`

Public paths (no auth): `/user/login`, `/user/sendCode`, `/user/register`, `/user/resetPassword`, `/prompt/list`

### API Response Pattern
All controllers return `ApiResponse<T>` from `com.aiimage.dto.ApiResponse`. Use `ApiResponse.success(data)` or `ApiResponse.error(message)`.

### Async Generation Tasks
`AsyncImageGenerator` handles task queuing. Generation flow: submit task → async processing → status polling via `GET /generation/{id}`.

### Database
MyBatis Plus with camel-case mapping. Druid connection pool configured in `application.yml`.

### Secrets
`sensitive` fields in `application.yml` (JWT secret, DB password, OSS keys, mail credentials) must not be committed.

## Frontend Structure

```
ai-art-platform/src/
├── api/          # Axios API calls
├── stores/       # Pinia state management
├── views/        # Page components
└── router/       # Vue Router config
```

Uses Element Plus UI, JWT stored in localStorage, JWT interceptor adds `Authorization: Bearer <token>` header.