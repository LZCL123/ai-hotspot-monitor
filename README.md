# AI热点监控工具

根据 `AI热点监控工具开发文档.md` 实现的全栈 MVP。

## 已实现范围

- 单用户登录，默认 `admin / admin123`
- 关键词订阅 CRUD，支持数据源、最低相关性和采集间隔配置
- HackerNews + Bing 统一采集器
- OpenRouter AI 分析；未配置 `OPENROUTER_API_KEY` 时使用本地规则兜底
- Redis 缓存关键词扩展和 AI 分析结果
- URL + 标题相似度去重
- 热点列表、详情、趋势统计
- Spring WebSocket/STOMP 实时推送
- 管理页查看数据源、采集日志、AI 调用日志
- Docker Compose 启动 MySQL、Redis、后端、前端

## 本地开发

后端：

```bash
cd backend
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

Docker：

```bash
docker compose up --build
```

前端 Docker 地址：`http://localhost:8088`

## 环境变量

复制 `.env.example` 为 `.env` 后按需填写：

- `APP_USERNAME`
- `APP_PASSWORD`
- `DASHSCOPE_API_KEY`
- `BAILIAN_MODEL`
- `BAILIAN_BASE_URL`
