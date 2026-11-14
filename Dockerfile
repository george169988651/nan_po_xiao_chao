# 构建前先在本机执行 mvn package,产物在 target/
FROM eclipse-temurin:17-jre

LABEL authors="George"
LABEL description="南坡小炒 Dockerfile"

ENV TZ=Asia/Shanghai
# 中文日志不乱码
ENV LANG=C.UTF-8

WORKDIR /app

# 直接用本机已经打好的 jar:不需要 maven 镜像,也不需要下载 Maven 依赖
COPY target/nan_po_xiao_chao-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /data/nanpo/images && chmod 755 /data/nanpo/images

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
