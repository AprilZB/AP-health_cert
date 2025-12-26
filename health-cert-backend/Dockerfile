# 员工健康证管理系统 - 后端服务Dockerfile
# 使用多阶段构建，减小镜像体积

# ==================== 第一阶段：Maven编译阶段 ====================
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/maven:3.8.6-openjdk-8 AS build

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件（利用Docker缓存层）
COPY pom.xml .

# 下载依赖（如果pom.xml未变化，此层会被缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 编译打包（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -B

# 验证JAR文件是否存在
RUN ls -la target/*.jar

# ==================== 第二阶段：JRE运行环境 ====================
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/openjdk:8-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
# Spring Boot打包后的JAR文件名格式：artifactId-version.jar
COPY --from=build /app/target/health-cert-backend-1.0.0.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/uploads /app/downloads /app/backups

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8"

# 暴露应用端口
EXPOSE 8080

# 启动应用
# 使用exec形式，确保Java进程作为PID 1运行，正确处理信号
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

