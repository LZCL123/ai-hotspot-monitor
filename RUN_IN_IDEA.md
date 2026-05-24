# 在 IDEA 里启动项目

这个项目分三部分启动：

1. 先启动依赖：MySQL，Redis
2. 再启动后端：`Backend - Spring Boot`
3. 最后启动前端：`Frontend - Vite dev`

## 依赖启动

当前这台机器已经配置成本机服务方式：

- MySQL：`localhost:3306`
- 数据库：`ai_hotspot`
- 用户名：`hotspot`
- 密码：`hotspot`
- Redis：`localhost:6379`

如果 Redis 没启动，可以执行：

```powershell
Start-Process -FilePath 'E:\redis\redis\redis-server.exe' -WindowStyle Hidden
```

也可以用 Docker Desktop：

```powershell
docker compose up -d mysql redis
```

注意：必须先打开 Docker Desktop，等 Docker Engine 启动完成后再执行上面的命令。

如果不用 Docker，在其他机器上需要自己准备：

- MySQL 8，端口 `3306`，或者把 IDEA 后端运行配置里的 `MYSQL_URL` 改成你的端口
- 数据库：`ai_hotspot`
- 用户名：`hotspot`
- 密码：`hotspot`
- Redis，端口 `6379`，无密码

后端会自动执行 `backend/src/main/resources/schema.sql` 建表。

## IDEA 运行项

我已经在 `.run/` 下放了共享运行配置。重新打开项目，或在 IDEA 右上角运行配置下拉框里刷新后，应能看到：

- `Backend - Spring Boot`
- `Backend - Maven spring-boot run`
- `Frontend - Vite dev`

优先用 `Backend - Spring Boot`。如果模块没有被 IDEA 正确识别，再用 `Backend - Maven spring-boot run`。

## 访问地址

- 后端：`http://localhost:8080`
- 前端：`http://localhost:5173`
- 默认账号：`admin`
- 默认密码：`admin123`

## 我这里验证到的情况

最开始直接运行后端时，Spring Boot 已经能启动到 Tomcat，但随后失败：

```text
Failed to obtain JDBC Connection
Connection refused
```

原因是默认配置连 Docker MySQL `localhost:3307`，但 Docker Desktop 后台没有启动。现在已改成使用本机 MySQL `localhost:3306`，并已创建项目需要的 `ai_hotspot` 数据库和 `hotspot/hotspot` 用户。
