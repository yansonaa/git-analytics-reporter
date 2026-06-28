# Git Analytics Reporter 部署手册

## 1. 环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ (前端开发) |
| Docker | 20+ (可选，用于容器化部署) |
| Docker Compose | 2+ (可选) |

## 2. 快速启动（Docker Compose）

```bash
# 1. 克隆项目
cd git-analytics-reporter

# 2. 启动基础设施（PostgreSQL + Redis）
docker-compose up -d postgres redis

# 3. 编译并启动应用
mvn clean package -DskipTests
docker-compose up -d app
```

## 3. 本地开发启动

### 3.1 后端
```bash
# 启动 PostgreSQL 和 Redis（如果本地没有）
docker-compose up -d postgres redis

# 运行 Spring Boot 应用
mvn spring-boot:run
```

### 3.2 前端
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:3000
```

## 4. 环境变量配置

| 变量名 | 默认值 | 说明 |
|--------|-------|------|
| `DB_HOST` | localhost | 数据库主机 |
| `DB_PORT` | 5432 | 数据库端口 |
| `DB_USER` | postgres | 数据库用户名 |
| `DB_PASSWORD` | postgres | 数据库密码 |
| `REDIS_HOST` | localhost | Redis 主机 |
| `REDIS_PORT` | 6379 | Redis 端口 |
| `GITLAB_TOKEN` | - | GitLab Private Token（AES加密存储） |
| `GITLAB_API_URL` | https://gitlab.com/api/v4 | GitLab API 地址 |
| `MAIL_HOST` | localhost | SMTP 服务器 |
| `MAIL_USERNAME` | - | 邮箱用户名 |
| `MAIL_PASSWORD` | - | 邮箱密码 |
| `REPORT_FROM_EMAIL` | reports@example.com | 报表发件人 |
| `REPORT_TO_EMAILS` | - | 报表收件人（逗号分隔） |

## 5. 数据库初始化

启动 PostgreSQL 后，schema 会自动通过 `docker-entrypoint-initdb.d` 执行 `schema.sql` 创建表和索引。

手动初始化：
```bash
psql -U postgres -d git_analytics -f src/main/resources/db/schema.sql
```

## 6. API 接口说明

### 6.1 报表查询
- **GET** `/api/reports/team?projectId={id}&start={iso}&end={iso}`
- 返回团队报表 JSON 数据

### 6.2 报表导出
- **GET** `/api/reports/team/export?projectId={id}&start={iso}&end={iso}`
- 下载 Excel 文件

### 6.3 数据采集
- **POST** `/api/collect/local?repoPath={path}&projectId={id}&since={iso}`
- 从本地 Git 仓库采集提交数据

### 6.4 监控端点
- **GET** `/actuator/health` - 健康检查
- **GET** `/actuator/prometheus` - Prometheus 指标

## 7. 定时任务

默认配置每月1号9:00自动发送上月报表邮件：
```yaml
app:
  report:
    cron: "0 0 9 1 * ?"
```

## 8. 前端构建

```bash
cd frontend
npm install
npm run build
# 构建产物位于 frontend/dist，可部署到 Nginx 或 CDN
```

## 9. 测试

```bash
# 运行单元测试
mvn test

# 生成测试覆盖率报告
mvn jacoco:report
# 报告位于 target/site/jacoco/index.html
```

## 10. 常见问题

**Q: 采集本地仓库失败？**  
A: 确保仓库路径正确，且包含 `.git` 目录。Windows 路径请使用 `E:/projects/my-repo` 格式。

**Q: 邮件发送失败？**  
A: 检查 SMTP 配置和认证信息。若不需要定时邮件，可将 `REPORT_TO_EMAILS` 留空。

**Q: 前端无法连接后端？**  
A: 检查 `vite.config.js` 中的 proxy 配置，确保指向正确的后端地址。
