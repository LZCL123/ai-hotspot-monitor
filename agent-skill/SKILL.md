---
name: ai-hotspot-monitor
description: Query and summarize AI industry hotspots from the AI Hotspot Monitor backend.
---

# AI Hotspot Monitor Skill

## Capabilities

- Query latest hotspots by keyword.
- Get hotspot detail and AI summary.
- Refresh subscriptions through the backend API.
- Generate a short daily topic briefing from returned hotspots.

## Inputs

- `baseUrl`: backend base URL, for example `http://localhost:8080`.
- `token`: login token from `POST /api/auth/login`.
- `keyword`: optional keyword filter.
- `importance`: optional `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL`.

## Outputs

Return JSON with:

```json
{
  "hotspots": [],
  "summary": "中文摘要",
  "errors": []
}
```

## Example

1. Login with `/api/auth/login`.
2. Query `/api/hotspots?keyword=AI%20Agent&size=10`.
3. Summarize titles, sources, relevance scores, and AI summaries.

## Error Handling

- If the token is missing or expired, login again.
- If the backend returns non-zero `code`, surface `message`.
- If no hotspots are found, return an empty list and a short explanation.
