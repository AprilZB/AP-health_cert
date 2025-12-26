# 员工健康证管理系统

## 📋 项目介绍

员工健康证管理系统是专为**浙江脉通智造科技(集团)有限公司**开发的健康证管理平台，支持员工健康证的上传、审核、到期提醒等功能。

### 主要功能

- ✅ **员工端**：健康证上传、OCR自动识别、历史记录查询
- ✅ **管理员端**：健康证审核、员工管理、数据统计、系统配置
- ✅ **自动化功能**：员工同步、到期提醒（邮件/钉钉）、数据备份
- ✅ **数据导出**：支持Excel/PDF格式导出

### 技术栈

- **后端**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3
- **数据库**：MySQL 8.0
- **前端**：纯HTML/CSS/JavaScript
- **部署**：Docker + Docker Compose + Nginx

---

## 🔧 环境要求

### 必需环境

- **Docker**：版本 20.10 或更高
- **Docker Compose**：版本 1.29 或更高
- **操作系统**：Linux / macOS / Windows（支持Docker）

### 系统资源要求

- **CPU**：2核心或以上
- **内存**：4GB或以上（推荐8GB）
- **磁盘空间**：至少10GB可用空间

### 检查环境

```bash
# 检查Docker版本
docker --version

# 检查Docker Compose版本
docker-compose --version

# 检查Docker服务状态
docker ps
```

---

## 🚀 快速开始

### 步骤1：克隆项目

```bash
# 克隆项目到本地
git clone <项目仓库地址>
cd health-cert-system
```

### 步骤2：配置环境变量（可选）

如果需要自定义配置，可以创建 `.env` 文件：

```bash
# 在项目根目录创建.env文件
cat > .env << EOF
# MySQL配置
MYSQL_ROOT_PASSWORD=root123
MYSQL_DATABASE=health_cert_db

# 后端服务配置
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/health_cert_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root123

# JWT配置（可选，默认使用application.yml中的配置）
JWT_SECRET=microport-health-cert-secret-2024
JWT_EXPIRATION=86400000
EOF
```

> **注意**：如果不创建`.env`文件，系统将使用`docker-compose.yml`和`application.yml`中的默认配置。

### 步骤3：启动服务

```bash
# 构建并启动所有服务（后台运行）
docker-compose up -d

# 查看服务启动日志
docker-compose logs -f
```

### 步骤4：等待服务启动

首次启动需要：
1. 下载Docker镜像（约5-10分钟，取决于网络速度）
2. 构建后端服务镜像（约3-5分钟）
3. 初始化MySQL数据库（约1-2分钟）

**总耗时约10-20分钟**，请耐心等待。

### 步骤5：验证服务状态

```bash
# 查看所有服务状态
docker-compose ps

# 应该看到三个服务都在运行：
# - health-cert-mysql (Up)
# - health-cert-backend (Up)
# - health-cert-nginx (Up)
```

---

## 🌐 访问地址和默认账号

### 访问地址

- **系统首页**：http://localhost
- **登录页面**：http://localhost/login.html
- **管理员工作台**：http://localhost/admin-dashboard.html
- **员工首页**：http://localhost/employee-home.html

### 默认管理员账号

- **用户名**：`admin`
- **密码**：`admin123`
- **角色**：超级管理员

> ⚠️ **安全提示**：首次登录后请立即修改默认密码！

### 端口说明

- **80端口**：Nginx反向代理（对外访问端口）
- **8080端口**：后端服务（仅用于调试，生产环境不对外开放）
- **3306端口**：MySQL数据库（仅用于调试，生产环境不对外开放）

---

## 📝 常用命令

### 服务管理

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 停止服务并删除数据卷（⚠️ 会删除所有数据）
docker-compose down -v

# 重启所有服务
docker-compose restart

# 重启指定服务
docker-compose restart backend
docker-compose restart mysql
docker-compose restart nginx
```

### 日志查看

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f backend
docker-compose logs -f mysql
docker-compose logs -f nginx

# 查看最近100行日志
docker-compose logs --tail=100 backend
```

### 服务状态

```bash
# 查看服务状态
docker-compose ps

# 查看服务资源使用情况
docker stats

# 进入容器内部（调试用）
docker-compose exec backend sh
docker-compose exec mysql bash
docker-compose exec nginx sh
```

### 数据库操作

```bash
# 连接MySQL数据库
docker-compose exec mysql mysql -uroot -proot123 health_cert_db

# 备份数据库
docker-compose exec mysql mysqldump -uroot -proot123 health_cert_db > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复数据库
docker-compose exec -T mysql mysql -uroot -proot123 health_cert_db < backup.sql
```

### 文件管理

```bash
# 查看上传文件
docker-compose exec backend ls -la /app/uploads

# 查看下载文件
docker-compose exec backend ls -la /app/downloads

# 查看备份文件
docker-compose exec backend ls -la /app/backups
```

### 镜像管理

```bash
# 重新构建后端镜像（代码更新后）
docker-compose build backend

# 重新构建并启动
docker-compose up -d --build backend

# 清理未使用的镜像和容器
docker system prune -a
```

---

## 🔍 故障排查

### 问题1：服务无法启动

**症状**：`docker-compose ps` 显示服务状态为 `Exit` 或 `Restarting`

**排查步骤**：

```bash
# 1. 查看服务日志
docker-compose logs backend
docker-compose logs mysql
docker-compose logs nginx

# 2. 检查端口占用
# Linux/macOS
netstat -tuln | grep -E '80|8080|3306'
# Windows
netstat -ano | findstr "80 8080 3306"

# 3. 检查磁盘空间
df -h  # Linux/macOS
# Windows在资源管理器中查看

# 4. 检查Docker服务状态
docker info
```

**常见原因**：
- 端口被占用：修改`docker-compose.yml`中的端口映射
- 磁盘空间不足：清理Docker镜像和容器
- 内存不足：增加系统内存或调整JVM参数

### 问题2：无法访问系统

**症状**：浏览器访问 http://localhost 显示无法连接

**排查步骤**：

```bash
# 1. 检查Nginx服务是否运行
docker-compose ps nginx

# 2. 检查Nginx日志
docker-compose logs nginx

# 3. 检查后端服务是否运行
docker-compose ps backend

# 4. 检查后端服务日志
docker-compose logs backend

# 5. 测试后端API
curl http://localhost:8080/api/auth/me
```

**常见原因**：
- Nginx未启动：执行 `docker-compose restart nginx`
- 后端服务未启动：执行 `docker-compose restart backend`
- 防火墙阻止：检查防火墙设置

### 问题3：数据库连接失败

**症状**：后端日志显示 `Communications link failure` 或 `Access denied`

**排查步骤**：

```bash
# 1. 检查MySQL服务是否运行
docker-compose ps mysql

# 2. 检查MySQL日志
docker-compose logs mysql

# 3. 测试MySQL连接
docker-compose exec mysql mysql -uroot -proot123 -e "SELECT 1"

# 4. 检查数据库是否创建
docker-compose exec mysql mysql -uroot -proot123 -e "SHOW DATABASES;"
```

**常见原因**：
- MySQL未完全启动：等待30-60秒后重试
- 密码错误：检查`docker-compose.yml`中的`MYSQL_ROOT_PASSWORD`
- 数据库未创建：检查初始化SQL脚本是否执行

### 问题4：登录失败

**症状**：使用默认账号登录时提示用户名或密码错误

**排查步骤**：

```bash
# 1. 检查数据库中的管理员账号
docker-compose exec mysql mysql -uroot -proot123 health_cert_db -e "SELECT username, password FROM admins;"

# 2. 检查初始化SQL是否执行
docker-compose exec mysql mysql -uroot -proot123 health_cert_db -e "SELECT COUNT(*) FROM admins;"

# 3. 手动初始化管理员账号（如果需要）
docker-compose exec mysql mysql -uroot -proot123 health_cert_db << EOF
INSERT INTO admins (username, password, real_name, email, role) 
VALUES ('admin', 'admin123', '超级管理员', 'admin@example.com', 'super_admin')
ON DUPLICATE KEY UPDATE password='admin123';
EOF
```

**常见原因**：
- 初始化SQL未执行：重新执行初始化脚本
- 密码被修改：使用数据库查询确认密码

### 问题5：文件上传失败

**症状**：上传健康证图片时提示错误

**排查步骤**：

```bash
# 1. 检查上传目录权限
docker-compose exec backend ls -la /app/uploads

# 2. 检查磁盘空间
docker-compose exec backend df -h

# 3. 检查Nginx配置
docker-compose exec nginx nginx -t

# 4. 查看后端日志
docker-compose logs backend | grep -i upload
```

**常见原因**：
- 目录权限不足：确保`/app/uploads`目录可写
- 磁盘空间不足：清理旧文件或增加磁盘空间
- 文件大小超限：检查Nginx的`client_max_body_size`配置

### 问题6：OCR识别失败

**症状**：上传图片后OCR识别返回空结果

**排查步骤**：

```bash
# 1. 检查OCR服务地址配置
docker-compose exec backend cat /app/application.yml | grep ocr

# 2. 测试OCR服务连通性
docker-compose exec backend wget -O- http://10.11.100.238:8081/predict

# 3. 查看后端日志
docker-compose logs backend | grep -i ocr
```

**常见原因**：
- OCR服务地址错误：检查`application.yml`中的`ocr.service.url`配置
- OCR服务不可访问：检查网络连接和防火墙设置
- 图片格式不支持：确保上传JPG/PNG/BMP格式

### 问题7：服务频繁重启

**症状**：`docker-compose ps` 显示服务状态为 `Restarting`

**排查步骤**：

```bash
# 1. 查看服务重启原因
docker-compose logs --tail=50 backend

# 2. 检查容器资源使用
docker stats health-cert-backend

# 3. 检查系统资源
# Linux
free -h
df -h
# Windows在任务管理器中查看
```

**常见原因**：
- 内存不足：增加JVM内存限制或系统内存
- 应用崩溃：查看日志找出错误原因
- 健康检查失败：检查健康检查配置

### 问题8：数据丢失

**症状**：重启后数据丢失

**排查步骤**：

```bash
# 1. 检查数据卷是否存在
docker volume ls | grep health-cert

# 2. 检查数据卷挂载
docker-compose config | grep volumes

# 3. 检查MySQL数据目录
docker-compose exec mysql ls -la /var/lib/mysql
```

**解决方案**：

```bash
# 如果数据卷丢失，需要重新创建
docker-compose down -v  # ⚠️ 这会删除所有数据
docker-compose up -d

# 从备份恢复数据
docker-compose exec -T mysql mysql -uroot -proot123 health_cert_db < backup.sql
```

---

## 📞 技术支持

如遇到其他问题，请：

1. 查看服务日志：`docker-compose logs -f`
2. 检查系统资源：CPU、内存、磁盘空间
3. 查看Docker状态：`docker info`
4. 联系技术支持团队

---

## 📄 许可证

本项目为内部系统，版权归浙江脉通智造科技(集团)有限公司所有。

---

## 📅 更新日志

### v1.0.0 (2024-xx-xx)
- ✅ 初始版本发布
- ✅ 支持健康证上传、审核、提醒功能
- ✅ Docker容器化部署

---

---

## 📚 项目文档

详细文档请查看 `docs/` 目录：

- 📖 [文档索引](./docs/README.md) - 文档导航
- 🔧 [技术文档](./docs/技术文档.md) - 技术栈、API接口、数据库设计
- 🏗️ [架构文档](./docs/架构文档.md) - 系统架构、技术架构、部署架构
- 🚀 [部署文档](./docs/部署文档.md) - Docker部署、环境配置、服务管理
- 📝 [更新说明](./docs/更新说明.md) - 版本历史、更新记录、升级指南
- ❓ [问题汇总](./docs/问题汇总.md) - 常见问题、故障排查、解决方案

---

**最后更新**：2024年12月26日

