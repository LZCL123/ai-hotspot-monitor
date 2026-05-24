# AI热点监控工具开发文档

## 1. 项目定位

AI热点监控工具是一个面向内容运营、AI开发者、产品经理或研究人员的热点信息聚合与智能分析系统。

系统通过多数据源采集热点内容，使用 AI 大模型进行真实性判断、相关性评分、重要性分级、摘要生成和关键词扩展，并通过 WebSocket / Socket.io 将新热点实时推送给订阅用户。

项目还需要支持将核心热点监控能力封装为 Agent Skills，方便在 Cursor、GitHub Copilot、Codex 等 AI 编程工具中复用。

## 2. 核心目标

1. 用户可以创建关键词监控任务。
2. 系统自动从多个数据源采集相关热点内容。
3. AI 对采集内容进行过滤、评分、摘要和分级。
4. 前端实时展示热点列表、详情、趋势和通知。
5. 后端通过 Socket.io / WebSocket 向订阅客户端推送新热点。
6. 系统支持缓存，避免重复采集和重复调用 AI。
7. 核心能力可以封装为 Agent Skill，供 AI 工具调用。

## 3. 建议技术栈

### 后端

- Java 17 或 Java 21
- Spring Boot 3.x
- Spring Web
- Spring Validation
- MyBatis-Plus
- MySQL 8.x
- Redis
- Socket.io Java 服务端或 Spring WebSocket
- Axios / Jsoup / Selenium / Playwright，按采集源复杂度选择
- OpenRouter API，用于统一接入 AI 大模型

### 前端

- Vue 3
- TypeScript
- Vite
- Pinia
- Vue Router
- Element Plus 或 Naive UI
- Socket.io Client
- ECharts，用于趋势图和统计图

### 部署

- Docker
- Docker Compose
- Nginx
- MySQL
- Redis

## 4. 前置准备

### 4.1 账号与密钥

开发前需要准备：

- OpenRouter API Key
- Twitter / X API 或第三方搜索 API Key，如果要接入 Twitter 高级搜索
- B站、HackerNews、Bing 等公开数据源不一定需要 Key，但要确认采集限制
- 如需部署 HTTPS，需要准备域名和 SSL 证书

所有密钥必须放在 `.env` 或后端环境变量中，不要写死在代码里。

### 4.2 本地环境

建议本地准备：

- JDK 17+
- Maven 3.8+
- Node.js 20+
- pnpm 或 npm
- MySQL 8+
- Redis 7+
- Docker Desktop，可选但推荐

### 4.3 合规与风控

爬虫采集前需要确认：

- 数据源是否允许抓取
- 是否有频率限制
- 是否需要 User-Agent
- 是否需要代理池
- 是否需要失败重试和退避机制
- 是否要保存原文，还是只保存摘要和链接

建议第一版只采集公开页面和官方开放 API，不做登录态采集，不绕过反爬。

## 5. 业务角色

### 普通用户

- 创建关键词订阅
- 查看热点列表
- 查看 AI 摘要和评分
- 接收实时推送

### 管理员

- 管理数据源
- 查看采集日志
- 配置 AI 模型
- 配置采集频率
- 管理失败任务

## 6. 核心功能模块

### 6.1 关键词订阅模块

功能：

- 创建关键词
- 设置监控频率
- 设置数据源范围
- 设置最低相关性分数
- 启用 / 停用订阅

字段建议：

- keyword
- expandedKeywords
- sourceTypes
- minRelevanceScore
- intervalMinutes
- enabled

### 6.2 数据采集模块

第一版建议支持：

- Bing Search
- HackerNews
- B站搜索
- Twitter / X 第三方 API，可选
- GitHub Trending 或 GitHub Search，可选
- 掘金 / 知乎 / 微博热榜，可选

采集器需要统一输出结构：

```json
{
  "source": "bing",
  "title": "文章标题",
  "url": "https://example.com",
  "summary": "原始摘要",
  "author": "作者，可为空",
  "publishedAt": "2026-05-20T12:00:00Z",
  "rawContent": "可选，正文或片段"
}
```

### 6.3 AI 分析模块

AI 需要完成：

- 判断内容是否真实可靠
- 判断内容是否与关键词相关
- 输出 0-100 的相关性分数
- 输出重要性等级：LOW / MEDIUM / HIGH / CRITICAL
- 生成中文摘要
- 提取标签
- 识别事件类型，例如产品发布、融资、漏洞、政策、开源项目、行业趋势

建议 AI 输出固定 JSON，便于后端解析。

```json
{
  "isValid": true,
  "relevanceScore": 86,
  "importance": "HIGH",
  "summary": "一句到三句话摘要",
  "reason": "为什么值得关注",
  "tags": ["AI", "开源", "模型"],
  "eventType": "产品发布"
}
```

### 6.4 查询扩展模块

用户输入一个关键词后，AI 自动扩展为 5 到 15 个语义变体。

示例：

用户输入：

```text
AI Agent
```

扩展结果：

```json
[
  "AI Agent",
  "Agentic AI",
  "autonomous agents",
  "AI workflow automation",
  "multi-agent system",
  "LLM agent framework"
]
```

扩展结果需要缓存，避免同一个关键词重复调用 AI。

### 6.5 缓存与去重模块

需要缓存：

- 查询扩展结果
- 已采集 URL
- AI 分析结果
- 热点列表

去重规则：

- URL 完全相同，直接去重
- 标题相似度高，可以判定为相同事件
- 同一个事件来自多个来源时，可以合并为一个热点，并记录来源列表

### 6.6 实时推送模块

用户订阅关键词后，后端在采集到新热点时推送消息。

事件建议：

- `hotspot:new`
- `hotspot:update`
- `subscription:status`
- `collector:error`

推送消息示例：

```json
{
  "event": "hotspot:new",
  "keyword": "AI Agent",
  "hotspotId": 1001,
  "title": "某公司发布新的 AI Agent 平台",
  "importance": "HIGH",
  "relevanceScore": 91,
  "summary": "简短摘要"
}
```

### 6.7 Agent Skill 模块

需要把热点监控能力封装成 AI 工具可理解的 Skill。

Skill 能力建议：

- 根据关键词查询最新热点
- 订阅关键词
- 获取热点摘要
- 获取热点趋势
- 对某个主题生成日报

Skill 文档中需要明确：

- 输入参数
- 输出格式
- 调用示例
- 错误处理

## 7. 数据库表设计草案

### user

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| username | varchar | 用户名 |
| password_hash | varchar | 密码哈希 |
| role | varchar | USER / ADMIN |
| created_at | datetime | 创建时间 |

### subscription

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| keyword | varchar | 原始关键词 |
| expanded_keywords | json | 扩展关键词 |
| source_types | json | 数据源 |
| min_relevance_score | int | 最低相关性 |
| interval_minutes | int | 采集间隔 |
| enabled | tinyint | 是否启用 |
| created_at | datetime | 创建时间 |

### hotspot

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| title | varchar | 标题 |
| url | varchar | 原始链接 |
| source | varchar | 来源 |
| keyword | varchar | 命中的关键词 |
| summary | text | AI 摘要 |
| relevance_score | int | 相关性分数 |
| importance | varchar | 重要性等级 |
| event_type | varchar | 事件类型 |
| tags | json | 标签 |
| published_at | datetime | 发布时间 |
| created_at | datetime | 入库时间 |

### collector_log

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| source | varchar | 数据源 |
| keyword | varchar | 关键词 |
| status | varchar | SUCCESS / FAILED |
| message | text | 日志信息 |
| started_at | datetime | 开始时间 |
| finished_at | datetime | 结束时间 |

### ai_analysis_log

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| provider | varchar | AI 服务商 |
| model | varchar | 模型 |
| input_hash | varchar | 输入哈希 |
| output_json | json | AI 输出 |
| cost_tokens | int | token 消耗 |
| created_at | datetime | 创建时间 |

## 8. 后端接口草案

### 订阅

```http
POST /api/subscriptions
GET /api/subscriptions
PUT /api/subscriptions/{id}
DELETE /api/subscriptions/{id}
POST /api/subscriptions/{id}/enable
POST /api/subscriptions/{id}/disable
```

### 热点

```http
GET /api/hotspots
GET /api/hotspots/{id}
GET /api/hotspots/trending
POST /api/hotspots/refresh
```

### AI

```http
POST /api/ai/expand-keywords
POST /api/ai/analyze
```

### 数据源

```http
GET /api/sources
PUT /api/sources/{id}
GET /api/collector-logs
```

## 9. 前端页面规划

### 首页 Dashboard

- 今日新增热点数
- 高重要性热点数
- 活跃订阅数
- 数据源健康状态
- 热点趋势图

### 关键词订阅页

- 订阅列表
- 新增订阅弹窗
- 数据源选择
- 最低相关性设置
- 启停开关

### 热点列表页

- 按关键词筛选
- 按重要性筛选
- 按来源筛选
- 按时间排序
- 实时新增提示

### 热点详情页

- 标题
- 来源
- 原始链接
- AI 摘要
- 相关性分数
- 重要性等级
- 标签
- 采集时间
- 相似热点

### 管理页

- 数据源配置
- AI 模型配置
- 采集日志
- AI 调用日志

## 10. 开发里程碑

### 第一阶段：MVP

- 后端基础工程
- MySQL 表结构
- Redis 配置
- 用户订阅 CRUD
- Bing / HackerNews 至少两个数据源采集
- OpenRouter AI 分析
- 热点列表接口
- Vue 基础页面

### 第二阶段：实时推送

- Socket.io / WebSocket 接入
- 关键词订阅房间
- 新热点实时推送
- 前端实时提示和列表更新

### 第三阶段：质量优化

- 查询扩展
- 去重合并
- AI 分析缓存
- 采集失败重试
- 日志与监控

### 第四阶段：Agent Skill

- 定义 Skill 输入输出
- 封装热点查询能力
- 编写 Skill README
- 提供调用示例

## 11. 给 AI 开发时的关键要求

请让 AI 严格遵守：

1. 不要把 API Key 写死在代码里。
2. 后端所有接口返回统一响应体。
3. 后端需要全局异常处理。
4. AI 输出必须是稳定 JSON，不能只返回自然语言。
5. 采集器必须实现统一接口，方便新增数据源。
6. 所有外部请求必须有超时、重试和错误日志。
7. 热点内容必须去重，不能重复刷屏。
8. WebSocket 推送要按用户订阅隔离。
9. 数据库字段要包含创建时间和更新时间。
10. 第一版不要做过度复杂的权限系统，普通登录即可。

## 12. 可直接发给 AI 的开发提示词

```text
你是一个资深全栈工程师，请根据以下需求开发一个“AI热点监控工具”。

技术栈要求：
- 后端：Java 17、Spring Boot 3、MyBatis-Plus、MySQL 8、Redis、OpenRouter API、Socket.io 或 Spring WebSocket
- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、ECharts、Socket.io Client
- 部署：Docker Compose、Nginx

项目目标：
开发一个 AI 驱动的热点监控系统。用户可以创建关键词订阅，系统自动从 Bing、HackerNews、B站等数据源采集相关内容，调用 OpenRouter 大模型进行真实性验证、相关性评分、重要性分级、中文摘要生成和标签提取，并将新热点通过 WebSocket 实时推送给订阅用户。

核心功能：
1. 用户登录和基础权限。
2. 关键词订阅 CRUD。
3. AI 查询扩展：将用户关键词扩展为 5 到 15 个语义相关查询词。
4. 多数据源采集：至少先实现 Bing 和 HackerNews，采集器必须使用统一接口。
5. AI 分析：调用 OpenRouter，要求输出固定 JSON，包括 isValid、relevanceScore、importance、summary、reason、tags、eventType。
6. 热点去重：基于 URL 和标题相似度去重。
7. 热点列表和详情接口。
8. WebSocket 实时推送：当采集到新热点时，推送给订阅对应关键词的客户端。
9. 管理端：查看采集日志、AI 调用日志、数据源状态。
10. Docker Compose 一键启动 MySQL、Redis、后端和前端。

工程要求：
- 后端使用分层架构：controller、service、mapper、entity、dto、vo、config、common、collector、ai、websocket。
- 所有接口使用统一响应体。
- 实现全局异常处理和参数校验。
- 外部 HTTP 请求必须配置超时、失败重试和日志。
- API Key 只能从环境变量读取。
- 数据库建表 SQL、后端 README、前端 README、Docker Compose 文件都要提供。
- 先完成 MVP，再逐步扩展，不要一次性写不可维护的大文件。

请先输出项目目录结构、数据库设计、接口设计和开发计划。等我确认后，再开始生成代码。
```

## 13. 需要你提前决定的问题

在正式开发前，建议先确定：

1. 是否必须接入 Twitter / X。如果必须，需要准备 API 或第三方服务。
2. 是否需要用户登录。如果只是个人工具，可以先用单用户配置。
3. 热点采集频率是多少。建议 MVP 阶段 10 到 30 分钟一次。
4. AI 模型选哪个。建议先用 OpenRouter 上成本较低、JSON 输出稳定的模型。
5. 是否需要保存原文全文。如果只做监控，建议先保存标题、链接、摘要、来源和 AI 结果。
6. Agent Skill 是第一版就要做，还是等 Web 系统稳定后再封装。

## 14. MVP 推荐范围

第一版建议只做这些：

- 单用户登录
- 关键词订阅
- Bing + HackerNews 采集
- OpenRouter 分析
- Redis 缓存
- 热点列表
- 热点详情
- Socket.io 实时推送
- Docker Compose 本地启动

先不要做：

- 复杂权限系统
- 代理池
- 登录态爬虫
- 太多数据源
- 复杂的 Agent Skill 市场适配
- 过度精细的统计报表

