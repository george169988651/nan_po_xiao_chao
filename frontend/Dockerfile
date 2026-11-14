# 构建前先在本机执行 npm run build,产物在 dist/
FROM nginx:1.25.5-alpine

# 删默认页面
RUN rm -rf /etc/nginx/html/*

# 前端文件
COPY dist /etc/nginx/html/

# 修权限：让 nginx worker 能读到文件
RUN chmod -R 755 /etc/nginx/html/

# 配置文件
COPY ./nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
