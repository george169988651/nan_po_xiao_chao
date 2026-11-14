FROM eclipse-temurin:17-jre

LABEL authors="George"
LABEL description="南坡小炒 Dockerfile"

# [问题10] 容器默认是 UTC,不设时区的话 LocalDateTime.now() 比北京时间早 8 小时
ENV TZ=Asia/Shanghai
# 中文日志不乱码
ENV LANG=C.UTF-8

# curl 供 docker-compose healthcheck 探活 /actuator/health 用
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY ./nan_po_xiao_chao-0.0.1-SNAPSHOT.jar app.jar

# 这个目录运行时会被 compose 的卷覆盖,这里只保证镜像里存在且权限够
RUN mkdir -p /data/nanpo/images && chmod 755 /data/nanpo/images

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
