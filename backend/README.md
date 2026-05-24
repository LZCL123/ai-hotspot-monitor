# AI热点监控工具后端

Spring Boot 3 MVP，包含单用户登录、关键词订阅、Bing/HackerNews 采集、OpenRouter AI 分析、Redis 缓存、热点接口和 WebSocket 推送。

## 本地启动

```bash
mvn spring-boot:run
```

默认配置读取：

- `MYSQL_URL=jdbc:mysql://localhost:3307/ai_hotspot...`
- `MYSQL_USER=root`
- `MYSQL_PASSWORD=root`
- `REDIS_HOST=localhost`
- `OPENROUTER_API_KEY=` 可为空；为空时使用本地规则兜底分析
- `APP_USERNAME=admin`
- `APP_PASSWORD=admin123`

## API

- `POST /api/auth/login`
- `GET|POST|PUT|DELETE /api/subscriptions`
- `POST /api/subscriptions/{id}/enable`
- `POST /api/subscriptions/{id}/disable`
- `GET /api/hotspots`
- `GET /api/hotspots/{id}`
- `GET /api/hotspots/trending`
- `POST /api/hotspots/refresh`
- `POST /api/ai/expand-keywords`
- `POST /api/ai/analyze`
- `GET /api/sources`
- `GET /api/collector-logs`
- `GET /api/ai-logs`

除登录外，接口使用 `Authorization: Bearer <token>`。
