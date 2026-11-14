# ===== 阶段一:构建——容器内用 Maven 编译,不依赖宿主机预先打好的 jar =====
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 先只拷 pom 下载依赖:这一层只要 pom 不变就命中缓存,改代码重建时快得多
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ===== 阶段二:运行——只要 JRE,镜像更小、攻击面更小 =====
FROM eclipse-temurin:17-jre

LABEL authors="George"
LABEL description="南坡小炒 Dockerfile"

ENV TZ=Asia/Shanghai
# 中文日志不乱码
ENV LANG=C.UTF-8

# curl 供 docker-compose healthcheck 探活 /actuator/health 用
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/nan_po_xiao_chao-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /data/nanpo/images && chmod 755 /data/nanpo/images

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
