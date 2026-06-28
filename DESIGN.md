# 系统设计文档

## 1. 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3 + ECharts)              │
│  Dashboard  ┃  趋势图  ┃  热力图  ┃  成员排名  ┃  异常列表    │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP REST /api
┌──────────────────────────┴──────────────────────────────────┐
│                    网关/控制层 (Spring Boot)                 │
│  ReportController ┃ CollectController ┃ Actuator            │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────┐
│                     服务层 (Service)                         │
│  GitCollectorService ┃ StatisticsEngine ┃ AnomalyDetector  │
│  ReportService ┃ ReportExportService ┃ ExportScheduler     │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────┐
│                     数据层 (Repository / JPA)               │
│  CommitRecordRepository ┃ ReviewRecordRepository            │
│  TeamMemberRepository                                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────┐
│                    基础设施层                                │
│  PostgreSQL  ┃  Redis  ┃  JGit  ┃  Mail SMTP  ┃  Prometheus  │
└─────────────────────────────────────────────────────────────┘
```

## 2. 核心模块职责

| 模块 | 职责 | 关键类 |
|------|------|--------|
| 数据采集 | 从本地 Git 仓库解析提交历史，统计 diff 行数 | `GitCollectorService` |
| 统计分析 | 计算趋势、热力图、代码量、活跃度评分 | `StatisticsEngine` |
| 异常检测 | 基于 Z-score 和时段规则识别异常提交 | `AnomalyDetector` |
| 报表生成 | 聚合数据为 JSON 报表，导出 Excel | `ReportService`, `ReportExportService` |
| 定时导出 | 每月1号自动生成并发送邮件 | `ExportScheduler` |

## 3. ER 图

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   commit_record │       │  review_record  │       │   team_member   │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ PK commit_id    │       │ PK id           │       │ PK email        │
│    project_id   │       │    mr_id        │       │    name         │
│    author_email │───────│    reviewer_email│◄─────│    team_id      │
│    author_name  │       │    review_time  │       │    is_active    │
│    commit_time  │       │    comment_count│       │    role         │
│    add_lines    │       │    response_sec │       └─────────────────┘
│    delete_lines │       │    project_id   │
│    net_lines    │       │    created_at   │
│    file_count   │       └─────────────────┘
│    message      │
│    is_merge     │
│    is_automated │
│    is_anomaly   │
│    created_at   │
└─────────────────┘
```

## 4. 接口文档

### 4.1 报表查询
- **URL**: `GET /api/reports/team`
- **参数**:
  - `projectId` (string, required): 项目标识
  - `start` (ISO datetime, required): 开始时间
  - `end` (ISO datetime, required): 结束时间
- **响应**: `ReportData` JSON

### 4.2 报表导出
- **URL**: `GET /api/reports/team/export`
- **参数**: 同报表查询
- **响应**: Excel 文件 (Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)

### 4.3 数据采集
- **URL**: `POST /api/collect/local`
- **参数**:
  - `repoPath` (string, required): 本地仓库路径
  - `projectId` (string, required): 项目标识
  - `since` (ISO datetime, optional): 起始时间，默认一个月前
- **响应**: `{ "success": true, "collected": 123 }`

## 5. 缓存策略

| 缓存名 | 键格式 | TTL | 说明 |
|--------|--------|-----|------|
| teamReport | `projectId:start:end` | 30 分钟 | 报表查询结果 |

## 6. 安全设计

- Git Token 使用 AES 加密存储
- 日志中对邮箱和 Token 进行脱敏（通过配置实现）
- 数据库连接池和 Redis 配置支持密码认证

## 7. 扩展性设计

- GitCollectorService 预留接口，后续可接入 GitLab API / GitHub GraphQL
- 报表引擎支持 JasperReports PDF 扩展（当前实现 Excel 优先）
- 前端组件化，支持新增图表类型

## 8. 监控指标

- `gitanalytics.method.timed` - 核心方法执行耗时（基于 AOP）
- Spring Boot Actuator `/actuator/prometheus` 暴露标准 JVM/HTTP 指标
- `jvm.memory.used`, `http.server.requests` 等可对接 Grafana
