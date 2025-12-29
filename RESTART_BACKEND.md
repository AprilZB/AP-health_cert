# 重启后端服务指南

## 问题说明
健康证详情接口返回404错误，需要重启后端服务使新代码生效。

## 解决方案

### 方法1：使用Docker Compose重启（推荐）

```bash
# 1. 重新构建后端镜像（包含最新代码）
docker-compose build backend

# 2. 重启后端服务
docker-compose restart backend

# 或者，停止并重新启动所有服务
docker-compose down
docker-compose up -d
```

### 方法2：仅重启后端容器

```bash
# 停止后端容器
docker stop health-cert-backend

# 删除旧容器
docker rm health-cert-backend

# 重新构建并启动
docker-compose up -d --build backend
```

### 方法3：查看日志确认服务状态

```bash
# 查看后端服务日志
docker-compose logs -f backend

# 查看最近50行日志
docker-compose logs --tail=50 backend
```

## 验证服务是否正常

### 1. 检查容器状态
```bash
docker ps | grep health-cert-backend
```

### 2. 检查健康检查端点
```bash
curl http://localhost:8080/api/auth/health
```

### 3. 测试详情接口（需要先登录获取token）
```bash
# 替换YOUR_TOKEN为实际的JWT token
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/health-cert/detail/1
```

## 常见问题

### Q: 重启后仍然404？
A: 检查以下几点：
1. 确认代码已提交并推送到Git
2. 确认Docker镜像已重新构建
3. 查看后端日志确认接口已注册
4. 检查路径是否正确：`/api/health-cert/detail/{id}`

### Q: 如何查看Spring Boot启动日志？
A: 
```bash
docker-compose logs -f backend | grep -i "mapping\|started"
```

### Q: 如何确认接口已注册？
A: 查看启动日志中是否有类似输出：
```
Mapped "{[/api/health-cert/detail/{id}],methods=[GET]}"
```

## 注意事项

1. **数据不会丢失**：重启容器不会影响数据库数据
2. **上传的文件**：如果使用volume挂载，文件会保留
3. **服务依赖**：后端重启时，Nginx会自动等待后端健康检查通过

